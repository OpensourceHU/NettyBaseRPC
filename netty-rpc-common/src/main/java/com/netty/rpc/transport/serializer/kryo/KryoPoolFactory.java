package com.netty.rpc.transport.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.netty.rpc.transport.DTO.RpcRequest;
import com.netty.rpc.transport.DTO.RpcResponse;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoPoolFactory {

  private static volatile KryoPoolFactory poolFactory = null;

  private KryoFactory factory = new KryoFactory() {
    @Override
    public Kryo create() {
      Kryo kryo = new Kryo();
      kryo.setReferences(false);
      kryo.register(RpcRequest.class);
      kryo.register(RpcResponse.class);
      Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
      strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
      return kryo;
    }
  };

  private KryoPool pool = new KryoPool.Builder(factory).build();

  private KryoPoolFactory() {
  }

  public static KryoPool getKryoPoolInstance() {
    if (poolFactory == null) {
      synchronized (KryoPoolFactory.class) {
        if (poolFactory == null) {
          poolFactory = new KryoPoolFactory();
        }
      }
    }
    return poolFactory.getPool();
  }

  public KryoPool getPool() {
    return pool;
  }
}
