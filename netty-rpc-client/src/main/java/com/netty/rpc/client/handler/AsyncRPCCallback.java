package com.netty.rpc.client.handler;

/**
 * Created by OpensourceHU on 2021-03-17.
 */
public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);

}
