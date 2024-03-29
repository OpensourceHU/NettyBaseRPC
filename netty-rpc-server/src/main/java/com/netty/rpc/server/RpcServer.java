package com.netty.rpc.server;

import com.netty.rpc.server.core.NettyServer;
import com.netty.rpc.service.annotation.NettyRpcService;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * RPC Server子类 集成Spring
 *
 * @author OpensourceHU
 */
public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean,
    DisposableBean {

  public RpcServer(String serverAddress, String registryAddress) {
    super(serverAddress, registryAddress);
  }

  /**
   * 服务器启动时 以扫描注解的方式进行服务注册
   *
   * @param ctx
   * @throws BeansException
   */
  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(NettyRpcService.class);
    if (MapUtils.isNotEmpty(serviceBeanMap)) {
      for (Object serviceBean : serviceBeanMap.values()) {
        NettyRpcService nettyRpcService = serviceBean.getClass()
            .getAnnotation(NettyRpcService.class);
        String interfaceName = nettyRpcService.value().getName();
        String version = nettyRpcService.version();
        super.addService(interfaceName, version, serviceBean);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.start();
  }

  @Override
  public void destroy() {
    super.stop();
  }
}
