package com.netty.rpc.client.core.connect;

import com.netty.rpc.client.core.handler.RpcClientHandler;
import com.netty.rpc.client.core.handler.RpcClientInitializer;
import com.netty.rpc.client.route.RpcLoadBalance;
import com.netty.rpc.client.route.normal.RpcLoadBalanceRoundRobin;
import com.netty.rpc.transport.IDL.RpcProtocol;
import com.netty.rpc.transport.IDL.RpcServiceInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Connection Manager Created by OpensourceHU on 2021-03-16.
 */
public class ConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

  private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8,
      600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

  public Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
  //写时复制集合 小数据量不用加锁； 相当于一个本地缓存，存上一次读取时ZK中RPC服务信息，每次读取ZK时更新
  private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();
  private ReentrantLock lock = new ReentrantLock();
  private Condition connected = lock.newCondition();
  private long waitTimeout = 5000;
  private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();

  public void setLoadBalance(RpcLoadBalance loadBalance) {
    this.loadBalance = loadBalance;
  }

  private volatile boolean isRunning = true;

  private ConnectionManager() {
  }

  private static class SingletonHolder {

    private static final ConnectionManager instance = new ConnectionManager();
  }

  public static ConnectionManager getInstance() {
    return SingletonHolder.instance;
  }

  public void updateConnectedServer(List<RpcProtocol> serviceList) {
    // Now using 2 collections to manage the service info and TCP connections because making the connection is async
    // Once service info is updated on ZK, will trigger this function
    // Actually client should only care about the service it is using
    if (serviceList != null && serviceList.size() > 0) {
      // Update local server nodes cache

      //List to Set
      Set<RpcProtocol> serviceSet = serviceList.stream().collect(Collectors.toSet());

//            // Add new server info
//            for (final RpcProtocol rpcProtocol : serviceSet) {
//                if (!rpcProtocolSet.contains(rpcProtocol)) {
//                    connectServerNode(rpcProtocol);
//                }
//            }

      //add new Server info
      serviceSet.stream()
          .filter((protocol) -> !rpcProtocolSet.contains(protocol))
          .forEach(this::connectServerNode);
//
//            for (RpcProtocol rpcProtocol : rpcProtocolSet) {
//                if (!serviceSet.contains(rpcProtocol)) {
//                    logger.info("Remove invalid service: " + rpcProtocol.toJson());
//                    removeAndCloseHandler(rpcProtocol);
//                }
//            }

      // Close and remove invalid server nodes
      rpcProtocolSet.stream()
          .filter(x -> !serviceSet.contains(x))
          .forEach(x ->
          {
            logger.info("Remove invalid service: " + x.toJson());
            removeHandler(x);
          });
    } else {
      // No available service
      logger.error("No available service!");
      for (RpcProtocol rpcProtocol : rpcProtocolSet) {
        removeAndCloseHandler(rpcProtocol);
      }
    }
  }


  public void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
    if (rpcProtocol == null) {
      return;
    }
    if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcProtocolSet.contains(rpcProtocol)) {
      connectServerNode(rpcProtocol);
    } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
      //TODO We may don't need to reconnect remote server if the server'IP and server'port are not changed
      removeAndCloseHandler(rpcProtocol);
      connectServerNode(rpcProtocol);
    } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
      removeAndCloseHandler(rpcProtocol);
    } else {
      throw new IllegalArgumentException("Unknow type:" + type);
    }
  }

  private void connectServerNode(RpcProtocol rpcProtocol) {
    if (rpcProtocol.getServiceInfoList() == null || rpcProtocol.getServiceInfoList().isEmpty()) {
      logger.info("No service on node, host: {}, port: {}", rpcProtocol.getHost(),
          rpcProtocol.getPort());
      return;
    }
    rpcProtocolSet.add(rpcProtocol);
    logger.info("New service node, host: {}, port: {}", rpcProtocol.getHost(),
        rpcProtocol.getPort());
    for (RpcServiceInfo serviceProtocol : rpcProtocol.getServiceInfoList()) {
      logger.info("New service info, name: {}, version: {}", serviceProtocol.getServiceName(),
          serviceProtocol.getVersion());
    }
    final InetSocketAddress remotePeer = new InetSocketAddress(rpcProtocol.getHost(),
        rpcProtocol.getPort());
    threadPoolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new RpcClientInitializer());

        ChannelFuture channelFuture = b.connect(remotePeer);
        channelFuture.addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(final ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
              logger.info("Successfully connect to remote server, remote peer = " + remotePeer);
              RpcClientHandler handler = channelFuture.channel().pipeline()
                  .get(RpcClientHandler.class);
              connectedServerNodes.put(rpcProtocol, handler);
              handler.setRpcProtocol(rpcProtocol);
              signalAvailableHandler();
            } else {
              logger.error("Can not connect to remote server, remote peer = " + remotePeer);
            }
          }
        });
      }
    });
  }

  private void signalAvailableHandler() {
    lock.lock();
    try {
      connected.signalAll();
    } finally {
      lock.unlock();
    }
  }

  private boolean waitingForHandler() throws InterruptedException {
    lock.lock();
    try {
      logger.warn("Waiting for available service");
      return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
    } finally {
      lock.unlock();
    }
  }

  public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
    int size = connectedServerNodes.values().size();
    while (isRunning && size <= 0) {
      try {
        waitingForHandler();
        size = connectedServerNodes.values().size();
      } catch (InterruptedException e) {
        logger.error("Waiting for available service is interrupted!", e);
      }
    }
    RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
    RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
    if (handler != null) {
      return handler;
    } else {
      throw new Exception("Can not get available connection");
    }
  }

  private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
    RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
    if (handler != null) {
      handler.close();
    }
    connectedServerNodes.remove(rpcProtocol);
    rpcProtocolSet.remove(rpcProtocol);
  }

  public void removeHandler(RpcProtocol rpcProtocol) {
    rpcProtocolSet.remove(rpcProtocol);
    connectedServerNodes.remove(rpcProtocol);
    logger.info("Remove one connection, host: {}, port: {}", rpcProtocol.getHost(),
        rpcProtocol.getPort());
  }

  public void stop() {
    isRunning = false;
    for (RpcProtocol rpcProtocol : rpcProtocolSet) {
      removeAndCloseHandler(rpcProtocol);
    }
    signalAvailableHandler();
    threadPoolExecutor.shutdown();
    eventLoopGroup.shutdownGracefully();
  }
}
