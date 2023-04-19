package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.IDL.RpcProtocol;
import com.netty.rpc.client.handler.RpcClientHandler;
import java.util.Map;
import java.util.Optional;

public class FailFast implements ExceptionRoute {

  //直接返回空（默认会打印堆栈信息）
  @Override
  public Optional<RpcClientHandler> doException(String serviceKey,
      Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
    return Optional.empty();
  }
}
