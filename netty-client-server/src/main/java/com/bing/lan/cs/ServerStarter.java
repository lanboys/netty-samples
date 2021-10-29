package com.bing.lan.cs;

import com.bing.lan.cs.server.NettyServer;

public class ServerStarter {

  public static void main(String[] args) throws Exception {
    //启动server服务
    new NettyServer().bind(12345);
  }
}
