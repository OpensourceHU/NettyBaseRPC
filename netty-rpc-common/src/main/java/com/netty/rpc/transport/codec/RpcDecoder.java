package com.netty.rpc.transport.codec;

import com.netty.rpc.transport.serializer.SerializerBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Decoder
 *
 * @author OpensourceHU
 */
public class RpcDecoder extends ByteToMessageDecoder {

  private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
  //待序列化类
  private Class<?> genericClass;
  //序列化方法类
  private SerializerBase serializerBase;

  public RpcDecoder(Class<?> genericClass, SerializerBase serializerBase) {
    this.genericClass = genericClass;
    this.serializerBase = serializerBase;
  }

  /**
   * 重写decode方法实现解码 处理入站信息
   *
   * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs
   *            to
   * @param in  the {@link ByteBuf} from which to read data
   * @param out the {@link List} to which decoded messages should be added
   * @throws Exception
   */
  @Override
  public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
      throws Exception {
    //长度小于4字节 为无效信息
    if (in.readableBytes() < 4) {
      return;
    }
    //提取出长度 如果可读字节小于长度 放弃读取
    in.markReaderIndex();
    int dataLength = in.readInt();
    if (in.readableBytes() < dataLength) {
      in.resetReaderIndex();
      return;
    }
    // 读取
    byte[] data = new byte[dataLength];
    in.readBytes(data);
    Object obj = null;
    try {
      obj = serializerBase.deserialize(data, genericClass);
      out.add(obj);
    } catch (Exception ex) {
      logger.error("Decode error: " + ex.toString());
    }
  }

}
