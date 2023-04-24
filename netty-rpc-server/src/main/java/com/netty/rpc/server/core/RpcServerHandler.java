package com.netty.rpc.server.core;

import com.netty.rpc.transport.DTO.RpcRequest;
import com.netty.rpc.transport.DTO.RpcResponse;
import com.netty.rpc.transport.codec.Beat;
import com.netty.rpc.util.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Handler（RPC request processor） 一个只处理 RPCrequest的Handler
 *
 * @author OpensourceHU
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

  private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

  private final Map<String, Object> handlerMap;

  private final ThreadPoolExecutor serverHandlerPool;

  public RpcServerHandler(Map<String, Object> handlerMap,
      final ThreadPoolExecutor threadPoolExecutor) {
    this.handlerMap = handlerMap;
    this.serverHandlerPool = threadPoolExecutor;
  }

  /**
   * once read data from channel forward to handle function do logging and reFlush buffer
   *
   * @param ctx     the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
   *                belongs to
   * @param request the message to handle
   */
  @Override
  public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest request) {
    // filter beat ping
    if (Beat.BEAT_ID.equalsIgnoreCase(request.getRequestId())) {
      logger.info("Server read heartbeat ping");
      return;
    }

    serverHandlerPool.execute(new Runnable() {
      @Override
      public void run() {
        logger.info("Receive request " + request.getRequestId());
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
          Object result = handle(request);
          response.setResult(result);
        } catch (Throwable t) {
          response.setError(t.toString());
          logger.error("RPC Server handle request error", t);
        }
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture channelFuture) throws Exception {
            logger.info("Send response for request " + request.getRequestId());
          }
        });
      }
    });
  }

  /**
   * get the service info from handlerMap use dynamic proxy to invoke the method
   *
   * @param request
   * @return
   * @throws Throwable
   */
  private Object handle(RpcRequest request) throws Throwable {
    //make service key
    String className = request.getClassName();
    String version = request.getVersion();
    String serviceKey = ServiceUtil.makeServiceKey(className, version);
    //find service via serviceKey
    Object serviceBean = handlerMap.get(serviceKey);
    if (serviceBean == null) {
      logger.error("Can not find service implement with interface name: {} and version: {}",
          className, version);
      return null;
    }
    //get service name and parameters info
    Class<?> serviceClass = serviceBean.getClass();
    String methodName = request.getMethodName();
    Class<?>[] parameterTypes = request.getParameterTypes();
    Object[] parameters = request.getParameters();
    //jdk reflect invoke method
    Method method = serviceClass.getMethod(methodName, parameterTypes);
    method.setAccessible(true);
    return method.invoke(serviceBean, parameters);

//        // Cglib reflect
//        FastClass serviceFastClass = FastClass.create(serviceClass);
//        // for higher-performance
//        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
//        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.warn("Server caught exception: " + cause.getMessage());
    ctx.close();
  }

  /**
   * default: invoke the next handler special judge : close idle channel
   *
   * @param ctx
   * @param evt
   * @throws Exception
   */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    //如果是闲置的 关闭channel
    if (evt instanceof IdleStateEvent) {
      ctx.channel().close();
      logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
