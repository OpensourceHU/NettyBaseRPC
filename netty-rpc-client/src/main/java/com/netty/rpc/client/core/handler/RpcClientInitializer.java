package com.netty.rpc.client.core.handler;

import com.netty.rpc.transport.DTO.RpcRequest;
import com.netty.rpc.transport.DTO.RpcResponse;
import com.netty.rpc.transport.codec.Beat;
import com.netty.rpc.transport.codec.RpcDecoder;
import com.netty.rpc.transport.codec.RpcEncoder;
import com.netty.rpc.transport.serializer.SerializerBase;
import com.netty.rpc.transport.serializer.kryo.KryoSerializerBase;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpensourceHU on 2021-03-16.
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    //默认序列化方式 serializerBase
    SerializerBase serializerBase = KryoSerializerBase.class.newInstance();
    ChannelPipeline cp = socketChannel.pipeline();
    //channelPipeline注册Handler
    cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
    cp.addLast(new RpcEncoder(RpcRequest.class, serializerBase));
    cp.addLast(new LengthFieldBasedFrameDecoder(65535 * 2, 0, 4, 0, 0));
    cp.addLast(new RpcDecoder(RpcResponse.class, serializerBase));
    cp.addLast(new RpcClientHandler());
  }
}
