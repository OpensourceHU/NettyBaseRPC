package com.app.test.functionTest.server;

import com.app.test.functionTest.service.CallBackService;
import com.app.test.functionTest.service.CallBackServiceImpl;
import com.app.test.functionTest.service.HelloService;
import com.app.test.functionTest.service.HelloServiceImpl;
import com.app.test.functionTest.service.HelloServiceImpl2;
import com.app.test.functionTest.service.PersonService;
import com.app.test.functionTest.service.PersonServiceImpl;
import com.netty.rpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServerBootstrap2 {

  private static final Logger logger = LoggerFactory.getLogger(RpcServerBootstrap2.class);

  public static void main(String[] args) {
    String serverAddress = "127.0.0.1:18877";
    String registryAddress = "127.0.0.1:2181";
    RpcServer rpcServer = new RpcServer(serverAddress, registryAddress);
    HelloService helloService1 = new HelloServiceImpl();
    rpcServer.addService(HelloService.class.getName(), "1.0", helloService1);
    HelloService helloService2 = new HelloServiceImpl2();
    rpcServer.addService(HelloService.class.getName(), "2.0", helloService2);
    PersonService personService = new PersonServiceImpl();
    rpcServer.addService(PersonService.class.getName(), "", personService);
    CallBackServiceImpl callBackService = new CallBackServiceImpl();
    rpcServer.addService(CallBackService.class.getName(), "1.0", callBackService);
    try {
      rpcServer.start();
    } catch (Exception ex) {
      logger.error("Exception: {}", ex.toString());
    }
  }
}
