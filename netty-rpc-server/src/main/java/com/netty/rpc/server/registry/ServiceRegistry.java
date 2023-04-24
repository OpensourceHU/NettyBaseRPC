package com.netty.rpc.server.registry;

import com.netty.rpc.service.ZK.CuratorClient;
import com.netty.rpc.service.cfg.ZKConstant;
import com.netty.rpc.transport.IDL.RpcProtocol;
import com.netty.rpc.transport.IDL.RpcServiceInfo;
import com.netty.rpc.util.ServiceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册
 *
 * @author OpensourceHU
 */
public class ServiceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
  //ZK Client
  private CuratorClient curatorClient;
  private List<String> pathList = new ArrayList<>();

  public ServiceRegistry(String registryAddress) {
    this.curatorClient = new CuratorClient(registryAddress, 5000);
  }

  public void registerService(String host, int port, Map<String, Object> serviceMap) {
    // Register service info
    List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
    for (String key : serviceMap.keySet()) {
      //设置服务名和版本号
      String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
      if (serviceInfo.length > 0) {
        RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
        rpcServiceInfo.setServiceName(serviceInfo[0]);
        if (serviceInfo.length == 2) {
          rpcServiceInfo.setVersion(serviceInfo[1]);
        } else {
          rpcServiceInfo.setVersion("");
        }
        logger.info("Register new service: {} ", key);
        serviceInfoList.add(rpcServiceInfo);
      } else {
        logger.warn("Can not get service name and version: {} ", key);
      }
    }
    //在当前的主机和端口下注册服务
    try {
      RpcProtocol rpcProtocol = new RpcProtocol();
      rpcProtocol.setHost(host);
      rpcProtocol.setPort(port);
      rpcProtocol.setServiceInfoList(serviceInfoList);
      //写入ZK节点
      String serviceData = rpcProtocol.toJson();
      byte[] bytes = serviceData.getBytes();
      String path = ZKConstant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
      path = this.curatorClient.createPathData(path, bytes);
      pathList.add(path);
      logger.info("Register {} new service, host: {}, port: {}", serviceInfoList.size(), host,
          port);
    } catch (Exception e) {
      logger.error("Register service fail, exception: {}", e.getMessage());
    }

    curatorClient.addConnectionStateListener(new ConnectionStateListener() {
      @Override
      public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        if (connectionState == ConnectionState.RECONNECTED) {
          logger.info("Connection state: {}, register service after reconnected", connectionState);
          registerService(host, port, serviceMap);
        }
      }
    });
  }

  public void unregisterService() {
    logger.info("Unregister all service");
    for (String path : pathList) {
      try {
        this.curatorClient.deletePath(path);
      } catch (Exception ex) {
        logger.error("Delete service path error: " + ex.getMessage());
      }
    }
    this.curatorClient.close();
  }
}
