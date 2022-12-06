package com.netty.rpc.server.core;

public abstract class Server {
    /**
     * start server
     *
     * @param
     * @throws Exception
     */
    public abstract void start() throws Exception;

    /**
     * stop server
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;

}
