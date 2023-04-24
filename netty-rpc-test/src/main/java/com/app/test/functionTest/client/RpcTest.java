package com.app.test.functionTest.client;

import com.app.test.functionTest.service.HelloService;
import com.netty.rpc.client.RpcClient;

/**
 * Created by OpensourceHU on 2021-03-11.
 */
public class RpcTest {

  public static void main(String[] args) throws InterruptedException {
    final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
//        int coreNum = Runtime.getRuntime().availableProcessors();
//        int threadNum = coreNum;
//        final int requestNum = 1000;
//        Thread[] threads = new Thread[threadNum];
//
    long startTime = System.currentTimeMillis();
    final HelloService helloService = rpcClient.createService(HelloService.class, "1.0");
    String result = helloService.hello("NettyRPC");
    System.out.println(result);
    //        //benchmark for sync call
//        for (int i = 0; i < threadNum; ++i) {
//            threads[i] = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < requestNum; i++) {
//                        try {
//                            final HelloService syncClient = rpcClient.createService(HelloService.class, "1.0");
//                            String result = syncClient.hello(Integer.toString(i));
////                            if (!result.equals("Hello " + i)) {
////                                System.out.println("error = " + result);
////                            } else {
////                                System.out.println("result = " + result);
////                            }
////                            try {
////                                Thread.sleep(5 * 1000);
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                        } catch (Exception ex) {
//                            System.out.println(ex.toString());
//                        }
//                    }
//                }
//            });
//            threads[i].start();
//        }
//        for (int i = 0; i < threads.length; i++) {
//            threads[i].join();
//        }
//    long timeCost = (System.currentTimeMillis() - startTime);
//    String msg = String.format("Sync call total-time-cost:%sms, req/s=%s", timeCost,
//        ((double) (requestNum * threadNum)) / timeCost * 1000);
//    System.out.println(msg);

    rpcClient.stop();
  }
}
