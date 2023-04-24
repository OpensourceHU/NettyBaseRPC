package com.netty.rpc.client.discovery;

import com.netty.rpc.client.core.connect.ConnectionManager;
import com.netty.rpc.service.ZK.CuratorClient;
import com.netty.rpc.service.cfg.ZKConstant;
import com.netty.rpc.transport.IDL.RpcProtocol;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用ZK实现服务发现
 *
 * @author OpensourceHU
 */
public class ServiceDiscovery {

  private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
  private CuratorClient curatorClient;

  public ServiceDiscovery(String registryAddress) {
    this.curatorClient = new CuratorClient(registryAddress);
    discoveryService();
  }

  private void discoveryService() {
    try {
      // Get initial service info
      logger.info("Get initial service info");
      getServiceAndUpdateServer();
      // Add watch listener
      curatorClient.watchPathChildrenNode(ZKConstant.ZK_REGISTRY_PATH,
          new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework,
                PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
              PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
              ChildData childData = pathChildrenCacheEvent.getData();
              switch (type) {
                case CONNECTION_RECONNECTED:
                  logger.info("Reconnected to zk, try to get latest service list");
                  getServiceAndUpdateServer();
                  break;
                case CHILD_ADDED:
                  getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                  break;
                case CHILD_UPDATED:
                  getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                  break;
                case CHILD_REMOVED:
                  getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                  break;
              }
            }
          });
    } catch (Exception ex) {
      logger.error("Watch node exception: " + ex.getMessage());
    }
  }

  private void getServiceAndUpdateServer() {
    try {
      List<String> nodeList = curatorClient.getChildren(ZKConstant.ZK_REGISTRY_PATH);
      List<RpcProtocol> dataList = new ArrayList<>();
      for (String node : nodeList) {
        logger.debug("Service node: " + node);
        byte[] bytes = curatorClient.getData(ZKConstant.ZK_REGISTRY_PATH + "/" + node);
        String json = new String(bytes);
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(json);
        dataList.add(rpcProtocol);
      }
      logger.debug("Service node data: {}", dataList);
      //Update the service info based on the latest data
      UpdateConnectedServer(dataList);
    } catch (Exception e) {
      logger.error("Get node exception: " + e.getMessage());
    }
  }

  private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
    String path = childData.getPath();
    String data = new String(childData.getData(), StandardCharsets.UTF_8);
    logger.info("Child data updated, path:{},type:{},data:{},", path, type, data);
    RpcProtocol rpcProtocol = RpcProtocol.fromJson(data);
    updateConnectedServer(rpcProtocol, type);
  }

  private void UpdateConnectedServer(List<RpcProtocol> dataList) {
    ConnectionManager.getInstance().updateConnectedServer(dataList);
  }


  private void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
    ConnectionManager.getInstance().updateConnectedServer(rpcProtocol, type);
  }

  public void stop() {
    this.curatorClient.close();
  }
}
