package com.netty.rpc.server.core;

import com.netty.rpc.server.registry.ServiceRegistry;
import com.netty.rpc.util.ServiceUtil;
import com.netty.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer extends Server {

  private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

  private Thread thread;
  private String serverAddress;
  private ServiceRegistry serviceRegistry;
  private Map<String, Object> serviceMap = new HashMap<>();

  public NettyServer(String serverAddress, String registryAddress) {
    this.serverAddress = serverAddress;
    this.serviceRegistry = new ServiceRegistry(registryAddress);
  }

  public void addService(String interfaceName, String version, Object serviceBean) {
    logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version,
        serviceBean);
    String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
    serviceMap.put(serviceKey, serviceBean);
  }

  public void start() {
    thread = new Thread(new Runnable() {
      ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(
          NettyServer.class.getSimpleName(), 16, 32);

      @Override
      public void run() {
        //指派Selector
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //工作组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
          //服务器启动器  初始化
          ServerBootstrap bootstrap = new ServerBootstrap();
          bootstrap.group(bossGroup, workerGroup)
              //设置ServerChannel类
              .channel(NioServerSocketChannel.class)
              //设置ChannelInitializer类
              .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
              //最大连接数
              .option(ChannelOption.SO_BACKLOG, 128)
              //开启长连接
              .childOption(ChannelOption.SO_KEEPALIVE, true);
          //解析出IP与端口
          String[] array = serverAddress.split(":");
          String host = array[0];
          int port = Integer.parseInt(array[1]);
          //同步绑定
          ChannelFuture future = bootstrap.bind(host, port).sync();
          if (serviceRegistry != null) {
            serviceRegistry.registerService(host, port, serviceMap);
          }
          logger.info("Server started on port {}", port);
          future.channel().closeFuture().sync();
        } catch (Exception e) {
          if (e instanceof InterruptedException) {
            logger.info("Rpc server remoting server stop");
          } else {
            logger.error("Rpc server remoting server error", e);
          }
        } finally {
          try {
            //释放资源 优雅停机
            serviceRegistry.unregisterService();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
          } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
          }
        }
      }
    });
    thread.start();
  }

  public void stop() {
    // destroy server thread
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }
  }

}
