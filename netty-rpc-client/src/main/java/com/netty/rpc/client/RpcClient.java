package com.netty.rpc.client;

import com.netty.rpc.client.core.connect.ConnectionManager;
import com.netty.rpc.client.core.proxy.ObjectProxy;
import com.netty.rpc.client.core.proxy.RpcService;
import com.netty.rpc.client.discovery.ServiceDiscovery;
import com.netty.rpc.service.annotation.RpcAutowired;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * RPC Client（Create RPC proxy）
 *
 * @author OpensourceHU
 */
public class RpcClient implements ApplicationContextAware, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

  private ServiceDiscovery serviceDiscovery;
  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
      600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

  public RpcClient(String address) {
    this.serviceDiscovery = new ServiceDiscovery(address);
  }

  @SuppressWarnings("unchecked")
  public static <T, P> T createService(Class<T> interfaceClass, String version) {
    return (T) Proxy.newProxyInstance(
        interfaceClass.getClassLoader(),
        new Class<?>[]{interfaceClass},
        new ObjectProxy<T, P>(interfaceClass, version)
    );
  }

  public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version) {
    return new ObjectProxy<T, P>(interfaceClass, version);
  }

  public static void submit(Runnable task) {
    threadPoolExecutor.submit(task);
  }

  public void stop() {
    threadPoolExecutor.shutdown();
    serviceDiscovery.stop();
    ConnectionManager.getInstance().stop();
  }

  @Override
  public void destroy() throws Exception {
    this.stop();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      Object bean = applicationContext.getBean(beanName);
      Field[] fields = bean.getClass().getDeclaredFields();
      try {
        for (Field field : fields) {
          RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
          if (rpcAutowired != null) {
            String version = rpcAutowired.version();
            field.setAccessible(true);
            field.set(bean, createService(field.getType(), version));
          }
        }
      } catch (IllegalAccessException e) {
        logger.error(e.toString());
      }
    }
  }
}

