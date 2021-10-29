package com.bing.lan.cs.test;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ByteBufTest {

  public static void main(String[] args) {
    int initialCapacity = 10;
    int maxCapacity = 50;
    ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(initialCapacity, maxCapacity);

    //print("allocate ByteBuf(" + initialCapacity + "," + maxCapacity + ")", buffer);
    write(buffer);

    //buffer.discardReadBytes();
    //buffer.discardSomeReadBytes();
    //
    //write(buffer);
  }

  private static void write(ByteBuf buffer) {
    // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写
    buffer.writeBytes(new byte[]{1, 2, 3, 4});
    print("writeBytes(1,2,3,4)", buffer);

    // 写完 int 类型之后，写指针增加4
    buffer.writeInt(8);
    print("writeInt(8)", buffer);

    // 写完之后写指针等于 capacity 的时候，buffer 不可写 isWritable(): false
    buffer.writeBytes(new byte[]{9, 10});
    print("writeBytes(9,10)", buffer);

    // 写的时候发现 buffer 不可写则开始扩容，扩容之后 capacity 随即改变
    buffer.writeBytes(new byte[]{11});
    print("writeBytes(11)", buffer);

    // get 方法不改变读写指针
    System.out.println("getByte(3) return: " + buffer.getByte(3));
    System.out.println("getShort(3) return: " + buffer.getShort(3));
    System.out.println("getInt(3) return: " + buffer.getInt(3));
    print("getByte()", buffer);

    // set 方法不改变读写指针
    buffer.setByte(buffer.readableBytes() + 1, 0);
    print("setByte()", buffer);

    // read 方法改变读指针
    byte[] dst = new byte[buffer.readableBytes() - 1];
    buffer.readBytes(dst);
    print("readBytes(" + dst.length + ")", buffer);
  }

  private static void print(String action, ByteBuf buffer) {
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
    Arrays.fill(dst, (byte) 'x');//为了测试，
    buffer.getBytes(buffer.readerIndex(), dst);

    System.out.println(Arrays.toString(dst));
    System.out.println();
  }
}
