package com.bing.lan.cs;

import com.bing.lan.cs.client.NettyClient;
import com.bing.lan.cs.protocol.RpcRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import io.netty.channel.Channel;

public class ClientStarter {

  public static void main(String[] args) throws Exception {
    NettyClient client = new NettyClient("127.0.0.1", 12345);
    //启动client服务
    client.start();

    Channel channel = client.getChannel();
    //消息体
    RpcRequest request = new RpcRequest();
    request.setId(UUID.randomUUID().toString());
    request.setData("client.message");
    //channel对象可保存在map中，供其它地方发送消息
    channel.writeAndFlush(request);

    int count = 0;
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        String str = bufferedReader.readLine();
        RpcRequest request1 = new RpcRequest();
        request1.setId(UUID.randomUUID().toString());
        StringBuilder sb = new StringBuilder();
        int i1 = 1000 + count * str.length() + count;
        for (int i = 0; i < 1; i++) {
          sb.append(str);
        }
        count++;
        request1.setData(sb.toString());
        channel.write(request1);
        System.out.println("write数据(还没有flush)：" + request1);

        if (count % 2 == 0) {
          channel.flush();
          System.out.println("flush数据");
        }
        //channel.writeAndFlush(request1);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
