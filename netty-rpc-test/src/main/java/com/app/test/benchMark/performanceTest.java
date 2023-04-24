package com.app.test.benchMark;

import com.app.test.functionTest.service.CallBackService;
import com.app.test.mock_util.StrGenerator;
import com.netty.rpc.client.RpcClient;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class performanceTest {

  //启用的线程数
  static final int coreSize = 8;
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

  public static String test() {
    CallBackService service = rpcClient.createService(CallBackService.class, "1.0");
    //generate str
    StrGenerator strGenerator = new StrGenerator(msgSize, "KB", msgArrLen);
    List<String> stringList = strGenerator.gen();
    AtomicInteger cnt = new AtomicInteger(0);
    long begin = System.currentTimeMillis();
    //submit task to threadPool
    CountDownLatch latch = new CountDownLatch(coreSize);
    for (int i = 0; i < coreSize; i++) {
      poolExecutor.submit(
          () -> {
            while (System.currentTimeMillis() - begin <= roundTime) {
              cnt.incrementAndGet();
              int idx = random.nextInt(stringList.size());
              String msg = stringList.get(idx);
              service.callBack(msg);
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
    //

    String s = String.format(
        "totally send %s request in %s seconds,total size %s KB, qps = %s, speed = %s KB/S",
        cnt.get(), timeCost, msgSize * cnt.get(), (double) cnt.get() / timeCost,
        msgSize * cnt.get() / timeCost).toString();
    return s;
  }


}
