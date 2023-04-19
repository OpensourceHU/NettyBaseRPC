package com.netty.rpc.client.route.exceptionRoute;

import com.netty.rpc.IDL.RpcProtocol;
import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.normal.RpcLoadBalanceRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Forking implements ExceptionRoute {

  private static RpcLoadBalanceRandom random = new RpcLoadBalanceRandom();

  private static int THREAD_COUNT = 10;

  private static int coreSize = Runtime.getRuntime().availableProcessors();

  public static void setThreadCount(int threadCount) {
    THREAD_COUNT = threadCount;
  }

  ThreadPoolExecutor poll = new ThreadPoolExecutor(coreSize, 2 * coreSize, 5, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(10));

  @Override
  public Optional<RpcClientHandler> doException(String serviceKey,
      Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
    List<FutureTask<RpcClientHandler>> arr = new ArrayList<>(THREAD_COUNT);
    for (int i = 0; i < THREAD_COUNT; i++) {
      arr.add(new FutureTask<RpcClientHandler>(() -> {
        RpcProtocol protocol = random.route(serviceKey, connectedServerNodes);
        return connectedServerNodes.get(protocol);
      }));
    }

    arr.stream().forEach(x -> poll.submit(x));
    while (poll.getActiveCount() != 0)
      ;
    poll.shutdown();
    Optional<FutureTask<RpcClientHandler>> result = arr.stream().filter(x -> x.isDone()).findAny();
    RpcClientHandler handler = null;
    try {
      handler = result.get().get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      handler = null;
      throw new RuntimeException(e);
    }
    return Optional.of(handler);
  }
}
