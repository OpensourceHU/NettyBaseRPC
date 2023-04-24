package com.netty.rpc.transport.serializer;

/**
 * 抽象序列化类 实现了三个 Hessian Kryo Protostuff
 */
public abstract class SerializerBase {

  public abstract <T> byte[] serialize(T obj);

  public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
