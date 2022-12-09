package com.netty.rpc.client.handler;

import com.netty.rpc.codec.*;
import com.netty.rpc.serializer.SerializerBase;
import com.netty.rpc.serializer.kryo.KryoSerializerBase;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import com.netty.rpc.DTO.RpcRequest;
import com.netty.rpc.DTO.RpcResponse;

import java.util.concurrent.TimeUnit;

/**
 * Created by OpensourceHU on 2021-03-16.
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //默认序列化方式 kryo
        SerializerBase kryo = KryoSerializerBase.class.newInstance();
        ChannelPipeline cp = socketChannel.pipeline();
        //channelPipeline注册Handler
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new RpcEncoder(RpcRequest.class, kryo));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class, kryo));
        cp.addLast(new RpcClientHandler());
    }
}
