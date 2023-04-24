package com.app.test.functionTest.client;

import com.app.test.functionTest.service.Person;
import com.app.test.functionTest.service.PersonService;
import com.netty.rpc.client.RpcClient;
import com.netty.rpc.client.core.handler.AsyncRPCCallback;
import com.netty.rpc.client.core.handler.RpcFuture;
import com.netty.rpc.client.core.proxy.RpcService;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by OpensourceHU on 2021/3/17.
 */
public class RpcCallbackTest {

  public static void main(String[] args) {
    final RpcClient rpcClient = new RpcClient("10.217.59.164:2181");
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    try {
      RpcService client = rpcClient.createAsyncService(PersonService.class, "");
      int num = 5;
      RpcFuture helloPersonFuture = client.call("callPerson", "Jerry", num);
      helloPersonFuture.addCallback(new AsyncRPCCallback() {
        @Override
        public void success(Object result) {
          List<Person> persons = (List<Person>) result;
          for (int i = 0; i < persons.size(); ++i) {
            System.out.println(persons.get(i));
          }
          countDownLatch.countDown();
        }

        @Override
        public void fail(Exception e) {
          System.out.println(e);
          countDownLatch.countDown();
        }
      });

    } catch (Exception e) {
      System.out.println(e);
    }

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    rpcClient.stop();

    System.out.println("End");
  }
}
