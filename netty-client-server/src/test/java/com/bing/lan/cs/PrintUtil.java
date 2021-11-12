package com.bing.lan.cs;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;

public class PrintUtil {

  public static void print(String action, ByteBuf buffer) {
    System.out.println(" ===========  " + action + "  ============");
    System.out.print("readerIndex(): " + buffer.readerIndex());
    System.out.print("  readableBytes(): " + buffer.readableBytes());
    System.out.print("  isReadable(): " + buffer.isReadable());
    System.out.print(" >>> writerIndex(): " + buffer.writerIndex());
    System.out.print("  writableBytes(): " + buffer.writableBytes());
    System.out.print("  isWritable(): " + buffer.isWritable());
    System.out.print(" >>> maxWritableBytes(): " + buffer.maxWritableBytes());
    System.out.print("  capacity(): " + buffer.capacity());
    System.out.println("  maxCapacity(): " + buffer.maxCapacity());

    byte[] dst = new byte[buffer.readableBytes()];
    buffer.getBytes(buffer.readerIndex(), dst);

    System.out.println(Arrays.toString(dst));
    System.out.println();
  }
}
