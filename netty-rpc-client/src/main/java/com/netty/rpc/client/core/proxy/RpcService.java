package com.netty.rpc.client.core.proxy;

import com.netty.rpc.client.core.handler.RpcFuture;

/**
 * Created by OpensourceHU on 2021/3/16.
 *
 * @author g-yu
 */
public interface RpcService<T, P, FN extends SerializableFunction<T>> {

  RpcFuture call(String funcName, Object... args) throws Exception;

  /**
   * lambda method reference
   */
  RpcFuture call(FN fn, Object... args) throws Exception;

}