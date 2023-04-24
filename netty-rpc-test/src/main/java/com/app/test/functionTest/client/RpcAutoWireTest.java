package com.app.test.functionTest.client;

import com.app.test.functionTest.service.HelloService;
import com.netty.rpc.client.RpcClient;
import com.netty.rpc.service.annotation.RpcAutowired;

public class RpcAutoWireTest {

  @RpcAutowired(version = "1.0")
  private HelloService helloService;

  private void say() {
    helloService.hello("hukaiyuan");
  }

  static RpcClient client = new RpcClient("127.0.0.1:2181");


}
