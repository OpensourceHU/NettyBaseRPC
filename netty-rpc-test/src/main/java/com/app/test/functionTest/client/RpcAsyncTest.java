package com.app.test.functionTest.client;

import com.app.test.functionTest.service.HelloService;
import com.netty.rpc.client.RpcClient;
import com.netty.rpc.client.core.handler.RpcFuture;
import com.netty.rpc.client.core.proxy.RpcService;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpensourceHU on 2021/3/16.
 */
public class RpcAsyncTest {

  public static void main(String[] args) throws InterruptedException {
    final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
    int coreNum = Runtime.getRuntime().availableProcessors();
    int threadNum = coreNum;
    final int requestNum = 1000;
    Thread[] threads = new Thread[threadNum];

    long startTime = System.currentTimeMillis();
    //benchmark for async call
    for (int i = 0; i < threadNum; ++i) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          for (int i = 0; i < requestNum; i++) {
            try {
              RpcService client = rpcClient.createAsyncService(HelloService.class, "2.0");
              RpcFuture helloFuture = client.call("hello", Integer.toString(i));
              String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
              System.out.println(e.toString());
            }
          }
        }
      });
      threads[i].start();
    }
    for (int i = 0; i < threads.length; i++) {
      threads[i].join();
    }
    long timeCost = (System.currentTimeMillis() - startTime);
    String msg = String.format("Async call total-time-cost:%sms, req/s=%s", timeCost,
        ((double) (requestNum * threadNum)) / timeCost * 1000);
    System.out.println(msg);

    rpcClient.stop();

  }
}
