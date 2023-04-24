package com.netty.rpc.transport.IDL;

import com.netty.rpc.util.JsonUtil;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 服务的主机\端口\相关信息 类
 */
public class RpcProtocol implements Serializable {

  private static final long serialVersionUID = -1102180003395190700L;
  // service host
  private String host;
  // service port
  private int port;
  // service info list
  private List<RpcServiceInfo> serviceInfoList;

  public String toJson() {
    String json = JsonUtil.objectToJson(this);
    return json;
  }

  public static RpcProtocol fromJson(String json) {
    return JsonUtil.jsonToObject(json, RpcProtocol.class);
  }

  /**
   * 判断RPCService集合是否相等  不关心元素顺序问题 也不关心是否重复
   *
   * @param thisList
   * @param thatList
   * @return
   */
  private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
    return thisList.containsAll(thatList) && thatList.containsAll(thisList);
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    RpcProtocol that = (RpcProtocol) o;
    return port == that.port &&
        Objects.equals(host, that.host) &&
        isListEquals(serviceInfoList, that.getServiceInfoList());
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, serviceInfoList.hashCode());
  }

  @Override
  public String toString() {
    return toJson();
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public List<RpcServiceInfo> getServiceInfoList() {
    return serviceInfoList;
  }

  public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
    this.serviceInfoList = serviceInfoList;
  }
}
