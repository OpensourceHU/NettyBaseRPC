package com.app.test.benchMark;

import com.app.test.functionTest.service.CallBackService;
import com.app.test.functionTest.service.HelloService;
import com.app.test.functionTest.service.Person;
import com.app.test.mock_util.PersonGen;
import com.app.test.mock_util.StrGenerator;
import com.netty.rpc.client.RpcClient;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class stableTest {


  //启用的线程数
  static final int coreSize = Runtime.getRuntime().availableProcessors();
  //单个消息的大小 单位KB
  static final int msgSize = 200;
  //消息队列长度
  static final int msgArrLen = 1000;
  //每轮测试持续时间 ms
  static final int roundTime = 1 * 60 * 1000;
  //随机数种子
  static Random random = new Random();
  //线程池
  static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(coreSize, 2 * coreSize, 5,
      TimeUnit.SECONDS,
      new LinkedBlockingDeque<>());
  static final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");

  static int mins = 0;

  static TreeMap<Integer, Object[]> map = new TreeMap<>();

  public static String test() {
    CallBackService callBackService = rpcClient.createService(CallBackService.class, "1.0");
    HelloService helloService = rpcClient.createService(HelloService.class, "2.0");
    //data init
    //generate str
    StrGenerator strGenerator = new StrGenerator(msgSize, "KB", msgArrLen);
    List<String> stringList = strGenerator.gen();
    AtomicInteger cnt = new AtomicInteger(0);
    //generate person
    List<Person> personList = new ArrayList<>();
    for (int i = 0; i < msgArrLen; i++) {
      Person person = PersonGen.genPerson(50);
      personList.add(person);
    }

    //bench test start
    long begin = System.currentTimeMillis();
    //submit task to threadPool
    CountDownLatch latch = new CountDownLatch(coreSize);
    for (int i = 0; i < coreSize; i++) {
      poolExecutor.submit(
          () -> {
            while (System.currentTimeMillis() - begin <= roundTime) {
              //call back
              int idx = random.nextInt(stringList.size());
              String msg = stringList.get(idx);
              callBackService.callBack(msg);

              //hello
              Person person = personList.get(idx);
              helloService.hello(person);

              cnt.incrementAndGet();
            }
            System.out.println("thread end latch countDown");
            latch.countDown();
          }
      );
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    //get time (ms)
    long end = System.currentTimeMillis();
    double timeCost = (double) (end - begin) / 1000;
    //data
    int reqNum = cnt.get() * 2;
    int totalSize = msgSize * cnt.get() + 50 * cnt.get();
    double tps = (double) cnt.get() * 2 / timeCost;
    double speed = totalSize / timeCost;
    map.put(++mins,
        new Object[]{String.valueOf(reqNum), String.valueOf(totalSize), String.valueOf(tps),
            String.valueOf(speed)});
    //
    String s = String.format(
        "totally send %s request in %s seconds,total size %s KB, Tps = %s, speed = %s KB/S",
        reqNum, timeCost, totalSize, tps, speed).toString();
    return s;
  }

  public static void writeData(Map<Integer, Object[]> map) {
    //Iterate over data and write to sheet
    Set<Integer> keySet = map.keySet();
    int rownum = 0;
    for (Integer key : keySet) {
      XSSFRow row = benchmarkData.createRow(rownum++);
      Object[] objArr = map.get(key);
      int cellnum = 0;
      for (Object obj : objArr) {
        XSSFCell cell = row.createCell(cellnum++);
        if (obj instanceof String) {
          cell.setCellValue((String) obj);
        } else if (obj instanceof Integer) {
          cell.setCellValue((Integer) obj);
        }
      }
    }
    try {
      //Write the workbook in file system
      FileOutputStream out = new FileOutputStream(new File("data.xlsx"));
      workbook.write(out);
      out.close();
      System.out.println("stableTest.xlsx written successfully on disk.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static XSSFWorkbook workbook = new XSSFWorkbook();
  static XSSFSheet benchmarkData = workbook.createSheet("benchmark Data");

  public static void main(String[] args) {
    workbook = new XSSFWorkbook();
    benchmarkData = workbook.createSheet("benchmark Data");
    for (int i = 0; i < 60; i++) {
      test();
    }
    writeData(map);

  }
//    ArrayList<String> result = new ArrayList<>();
//    //测试持续60mins
//    for (int i = 0; i < 1; i++) {
//      String oneMinutesResult = test();
//      System.out.println(oneMinutesResult);
//      result.add(oneMinutesResult);
//    }
//    System.out.println("============serialization: kryo benchmark test result============");
//    for (String s : result
//    ) {
//      System.out.println(s);
//    }


}
