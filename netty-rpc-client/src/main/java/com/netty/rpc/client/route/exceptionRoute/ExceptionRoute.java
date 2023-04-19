package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.IDL.RpcProtocol;
import com.netty.rpc.client.handler.RpcClientHandler;
import java.util.Map;
import java.util.Optional;

/**
 * 当出现异常时的重试策略
 */
public interface ExceptionRoute {

  Optional<RpcClientHandler> doException(String serviceKey,
      Map<RpcProtocol, RpcClientHandler> connectedServerNodes);
}
