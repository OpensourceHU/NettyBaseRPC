package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.client.core.handler.RpcClientHandler;
import com.netty.rpc.client.route.normal.RpcLoadBalanceRandom;
import com.netty.rpc.transport.IDL.RpcProtocol;
import java.util.Map;
import java.util.Optional;

public class FailOver implements ExceptionRoute {

  private static RpcLoadBalanceRandom random = new RpcLoadBalanceRandom();
  private static int MAX_RETRY = 5;

  public static void setMaxRetry(int maxRetry) {
    MAX_RETRY = maxRetry;
  }

  @Override
  public Optional<RpcClientHandler> doException(String serviceKey,
      Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
    for (int i = 0; i < MAX_RETRY; i++) {
      RpcProtocol protocol = null;
      try {
        protocol = random.route(serviceKey, connectedServerNodes);
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }
      if (protocol != null) {
        RpcClientHandler handler = connectedServerNodes.get(protocol);
        return Optional.of(handler);
      }
    }

    return Optional.empty();
  }
}
