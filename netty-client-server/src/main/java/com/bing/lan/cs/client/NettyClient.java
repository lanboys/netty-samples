package com.bing.lan.cs.client;

import com.bing.lan.cs.protocol.RpcDecoder;
import com.bing.lan.cs.protocol.RpcEncoder;
import com.bing.lan.cs.protocol.RpcRequest;
import com.bing.lan.cs.protocol.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

  private final String host;
  private final int port;
  private Channel channel;

  //连接服务端的端口号地址和端口号
  public NettyClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start() throws Exception {
    final EventLoopGroup group = new NioEventLoopGroup();

    Bootstrap b = new Bootstrap();
    b.group(group)
        .channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
        .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
          @Override
          public void initChannel(SocketChannel ch) throws Exception {
            System.out.println("正在连接中...");
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new RpcEncoder(RpcRequest.class)); //编码request
            pipeline.addLast(new RpcDecoder(RpcResponse.class)); //解码response
            pipeline.addLast(new ClientHandler()); //客户端处理类
          }
        });
    //发起异步连接请求，绑定连接端口和host信息
    //sync() 等待异步连接的结果。
    final ChannelFuture future = b.connect(host, port).sync();

    if (future.isSuccess()) {
      System.out.println("连接服务器成功");
    } else {
      System.out.println("连接服务器失败");
      future.cause().printStackTrace();
      group.shutdownGracefully(); //关闭线程组
    }

    this.channel = future.channel();
  }

  public Channel getChannel() {
    return channel;
  }
}
