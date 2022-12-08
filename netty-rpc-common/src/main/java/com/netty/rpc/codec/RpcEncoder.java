package com.netty.rpc.codec;

import com.netty.rpc.serializer.SerializerBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Encoder
 *
 * @author OpensourceHU
 */
public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private Class<?> genericClass;
    private SerializerBase serializerBase;

    public RpcEncoder(Class<?> genericClass, SerializerBase serializerBase) {
        this.genericClass = genericClass;
        this.serializerBase = serializerBase;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            try {
                byte[] data = serializerBase.serialize(in);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex) {
                logger.error("Encode error: " + ex.toString());
            }
        }
    }
}
