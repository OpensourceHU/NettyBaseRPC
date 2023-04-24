package com.netty.rpc.server.core;

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
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

  private Map<String, Object> handlerMap;
  private ThreadPoolExecutor threadPoolExecutor;

  public RpcServerInitializer(Map<String, Object> handlerMap,
      ThreadPoolExecutor threadPoolExecutor) {
    this.handlerMap = handlerMap;
    this.threadPoolExecutor = threadPoolExecutor;
  }

  @Override
  public void initChannel(SocketChannel channel) throws Exception {
    SerializerBase serializerBase = KryoSerializerBase.class.newInstance();
    ChannelPipeline cp = channel.pipeline();
    cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
    cp.addLast(new LengthFieldBasedFrameDecoder(65535 * 2, 0, 4, 0, 0));
    cp.addLast(new RpcDecoder(RpcRequest.class, serializerBase));
    cp.addLast(new RpcEncoder(RpcResponse.class, serializerBase));
    cp.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
  }
}
