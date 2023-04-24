package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.client.core.handler.RpcClientHandler;
import com.netty.rpc.transport.IDL.RpcProtocol;
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
