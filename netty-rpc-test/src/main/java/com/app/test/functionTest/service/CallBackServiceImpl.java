package com.app.test.functionTest.service;

import com.netty.rpc.service.annotation.NettyRpcService;

@NettyRpcService(value = CallBackService.class, version = "1.0")
public class CallBackServiceImpl implements CallBackService {

  @Override
  public String callBack(String msg) {
    return msg;
  }
}
