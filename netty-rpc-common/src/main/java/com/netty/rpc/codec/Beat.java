package com.netty.rpc.codec;

import com.netty.rpc.service.RpcRequest;

/**
 * 心跳包
 */
public final class Beat {
    //间隔
    public static final int BEAT_INTERVAL = 30;
    //超时时间
    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;
    /**
     * 通过发送特殊的RPC请求(ID 为 BEAT_PING_PONG) 来维持心跳
     */
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest() {};
        BEAT_PING.setRequestId(BEAT_ID);
    }

}
