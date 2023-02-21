# Netty_Base_RPC
一款基于Netty的轻量级RPC框架， 计网课设



整体架构![系统架构图](assets/%E6%9C%AA%E5%91%BD%E5%90%8D%E6%96%87%E4%BB%B6.png)




新增功能： 

1.   支持Spring注解方式实现注册
2.   支持不同的序列化方式 Hessian Kryo Protostuff
3.   支持不同的loadBalence策略 LRU LFU RoundRobin
4.   新增心跳连接检测功能
5.   支持异步调用



待完善： loadBalence策略与序列化方式支持配置



使用方法：

1. 配置Zookeeper环境, 推荐用Docker

    ```shell
    #拉取镜像
    docker pull zookeeper
    #部署在本机2181端口
    docker run -d -e TZ="Asia/Shanghai" -p 2181:2181 -v $PWD/data:/data --name zookeeper --restart always zookeeper
    ```

2. 修改rpc.properties中的配置项，测试时默认注册中心为本机2181端口，服务器地址为本机18866端口

    ```xml
    # zookeeper server
    registry.address = 127.0.0.1:2181
    # rpc server
    server.address = 127.0.0.1:18866
    ```

3. 启动服务器（测试环境下运行RpcServerBootstrap类即可） 打上了NettyRpcService注解的类会自动注册

4. 在client端调用即可

    ```java
    final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
    	
    // Sync call
    HelloService helloService = rpcClient.createService(HelloService.class, "1.0");
    String result = helloService.hello("World");
    	
    // Async call
    RpcService client = rpcClient.createAsyncService(HelloService.class, "2.0");
    RPCFuture helloFuture = client.call("hello", "World");
    String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
    ```

    或者使用RpcAutowired注解

    ```java
    @RpcAutowired(version = "1.0")
    private HelloService helloService1;
    
    @RpcAutowired(version = "2.0")
    private HelloService helloService2;
    
    @Override
    public String say(String s) {
        return helloService1.hello(s);
    }
    ```

5. 在Test包内有两个类可以简单测试一下RPCTest(同步调用) RPCAsyncTest(异步调用)

    启动当前主机核心数为线程数,单个线程的请求数量可以自定义(暂时设置为1000)

    测试结果如下--本地测试,忽略网络延迟;服务端接收消息立即返回的情况下:    

    ```
    Async call total-time-cost:911ms, req/s=26344.676180021954
    Sync call total-time-cost:1014ms, req/s=23668.63905325444
    ```

     

      

