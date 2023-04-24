package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.client.core.handler.RpcClientHandler;
import com.netty.rpc.transport.IDL.RpcProtocol;
import java.util.Map;
import java.util.Optional;

/**
 * 当出现异常时的重试策略
 */
public interface ExceptionRoute {

  Optional<RpcClientHandler> doException(String serviceKey,
      Map<RpcProtocol, RpcClientHandler> connectedServerNodes);
}
