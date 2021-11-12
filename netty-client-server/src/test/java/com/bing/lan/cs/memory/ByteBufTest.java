package com.bing.lan.cs.memory;

import com.bing.lan.cs.PrintUtil;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * PooledUnsafeDirectByteBuf底层使用的ByteBuffer, 通过ByteBuffer分配缓存，但是读写的时候没有使用它自己
 * 的api，而是直接使用Unsafe工具来读写，可以debug两个类的读取方法进行测试
 */
public class ByteBufTest {

  @Test
  public void testByteBufWrite() {

    int initialCapacity = 1 * 1024 * 1024;
    int maxCapacity = 5 * 1024 * 1024;
    //ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(initialCapacity, maxCapacity);
    //ByteBuf pooledDirectBuffer = ByteBufAllocator.DEFAULT.directBuffer(initialCapacity, maxCapacity);
    ByteBuf pooledHeapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(initialCapacity, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(initialCapacity, maxCapacity);

    //ByteBuf unPooledDirectBuffer = UnpooledByteBufAllocator.DEFAULT.directBuffer(initialCapacity, maxCapacity);
    //ByteBuf unPooledHeapBuffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(initialCapacity, maxCapacity);

    //PrintUtil.print("allocate ByteBuf(" + initialCapacity + "," + maxCapacity + ")", buffer);
    //write(pooledDirectBuffer);
    write(pooledHeapBuffer);

    //write(unPooledDirectBuffer);
    //write(unPooledHeapBuffer);

    //buffer.discardReadBytes();
    //buffer.discardSomeReadBytes();
    //
    //write(buffer);
  }

  private static void write(ByteBuf buffer) {
    // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写
    buffer.writeBytes(new byte[]{1, 2, 3, 4});
    PrintUtil.print("writeBytes(1,2,3,4)", buffer);

    // 写完 int 类型之后，写指针增加4
    buffer.writeInt(8);
    PrintUtil.print("writeInt(8)", buffer);

    // 写完之后写指针等于 capacity 的时候，buffer 不可写 isWritable(): false
    buffer.writeBytes(new byte[]{9, 10});
    PrintUtil.print("writeBytes(9,10)", buffer);

    // 写的时候发现 buffer 不可写则开始扩容，扩容之后 capacity 随即改变
    buffer.writeBytes(new byte[]{11});
    PrintUtil.print("writeBytes(11)", buffer);

    // get 方法不改变读写指针
    System.out.println("getByte(3) return: " + buffer.getByte(3));
    System.out.println("getShort(3) return: " + buffer.getShort(3));
    System.out.println("getInt(3) return: " + buffer.getInt(3));
    PrintUtil.print("getByte()", buffer);

    // set 方法不改变读写指针
    buffer.setByte(buffer.readableBytes() + 1, 0);
    PrintUtil.print("setByte()", buffer);

    // read 方法改变读指针
    byte[] dst = new byte[buffer.readableBytes() - 1];
    buffer.readBytes(dst);
    PrintUtil.print("readBytes(" + dst.length + ")", buffer);
  }
}
