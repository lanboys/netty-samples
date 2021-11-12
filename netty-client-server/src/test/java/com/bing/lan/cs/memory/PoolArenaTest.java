package com.bing.lan.cs.memory;

import com.bing.lan.cs.PrintUtil;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

//pooledHeapBuffer.release();

/**
 * https://zhuanlan.zhihu.com/p/100239049
 * https://zhuanlan.zhihu.com/p/259819465
 */
public class PoolArenaTest {

  /**
   * 测试正常内存块分配，大于等于 8k
   */
  @Test
  public void testNormal() {
    int maxCapacity = 10 * 1024 * 1024; // 10m

    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(4 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(2 * 1024 * 1024, maxCapacity);
    pooledHeapBuffer1.release();
    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(4 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer4 = ByteBufAllocator.DEFAULT.heapBuffer(1 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer5 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024 * 1024, maxCapacity);

    PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer3);
    PrintUtil.print("pooledHeapBuffer4", pooledHeapBuffer4);
    PrintUtil.print("pooledHeapBuffer5", pooledHeapBuffer5);
  }

  /**
   * 测试小内存块 tiny / small分配，两者原理都是一样的
   */
  @Test
  public void testSubpage() {
    int maxCapacity = 10 * 1024;// 10k
    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer4 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer5 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);

    PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer3);
    PrintUtil.print("pooledHeapBuffer4", pooledHeapBuffer4);
    PrintUtil.print("pooledHeapBuffer5", pooledHeapBuffer5);
  }

  /**
   * 测试扩容
   */
  @Test
  public void testCapacity() {
    int maxCapacity = 10 * 1024;// 10k
    ByteBuf pooledHeapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    for (int i = 0; i < 3 * 1024 - 1; i++) {
      pooledHeapBuffer.writeByte(1);
    }
    pooledHeapBuffer.writeByte(1);// 满3k, 再次写入就需要扩容
    pooledHeapBuffer.writeByte(1);// 会扩容到4k, 原来的Capacity, 还没达到4k, 但是Subpage内存块是4k还足够使用, 所以只改变指针，没有重新分配内存
    for (int i = 0; i < 1024 - 1; i++) {
      pooledHeapBuffer.writeByte(1);
    }
    // 此时已满4k, 再次写入就需要扩容
    pooledHeapBuffer.writeByte(1);// 会扩容到8k, 4k 的Subpage内存已经不够, 需要重新分配
    pooledHeapBuffer.writeByte(1);
    pooledHeapBuffer.writeByte(1);

    PrintUtil.print("pooledHeapBuffer", pooledHeapBuffer);
  }

  /**
   * 测试内存块不再使用后，是被缓存起来了，等待下一次被分配
   */
  @Test
  public void testPoolThreadCache() {
    int maxCapacity = 10 * 1024;// 10k
    ByteBuf pooledHeapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);
    for (int i = 0; i < 4 * 1024 - 1; i++) {
      pooledHeapBuffer.writeByte(1);// 期间有过一次扩容
    }
    pooledHeapBuffer.writeByte(1);// 此时已满4k, 再次写入就需要扩容到8k，内存块将变成Normal类型了，原来的4k内存块将被缓存

    pooledHeapBuffer.writeByte(1);// 会扩容到8k，原来的4k内存块被缓存
    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);// 将从缓存中分配4k内存块

    PrintUtil.print("pooledHeapBuffer", pooledHeapBuffer);
    PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer3);
  }

  /**
   * PooledByteBufAllocator 里面很多jvm配置
   * 取消线程缓存 -Dio.netty.allocator.useCacheForAllThreads=false
   * 测试释放小内存
   */
  @Test
  public void testReleasePoolSubpage() {
    int maxCapacity = 10 * 1024;// 10k
    ByteBuf pooledHeapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);// 从第一个PoolSubpage分配4k
    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);//  仍然从第一个PoolSubpage分配4k，并将PoolSubpage从smallSubpagePools中移除
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);//  从第二个PoolSubpage分配4k

    // 按上面参数设置为线程不进行缓存，释放内存后，将PoolSubpage重新添加到smallSubpagePools中，
    // 此时4k这组链表中有两个PoolSubpage
    pooledHeapBuffer.release();
    // 释放内存后，该PoolSubpage将交还给上一级PoolChunk，等待下一次被分配，但是被存在PoolChunk.subpages中
    // 的PoolSubpage对象并没有销毁，下次仍然可能再被使用(非常谨慎new/销毁对象，各种对象被复用，把性能优化到极致了)
    // 此时4k这组链表中剩下一个PoolSubpage
    pooledHeapBuffer1.release();

    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);// 从上面的第二个PoolSubpage分配4k，然后将PoolSubpage从smallSubpagePools中移除
    ByteBuf pooledHeapBuffer4 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024, maxCapacity);// 重新从上面的第一个PoolSubpage分配4k，PoolSubpage对象此时也被复用

    //PrintUtil.print("pooledHeapBuffer", pooledHeapBuffer);
    //PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer4", pooledHeapBuffer2);
  }

  /**
   * 取消线程缓存 -Dio.netty.allocator.useCacheForAllThreads=false
   * 测试释放正常内存，同上面的小内存释放差不多
   */
  @Test
  public void testReleaseNormal() {
    int maxCapacity = 10 * 1024 * 1024; // 10m

    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(4 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(2 * 1024 * 1024, maxCapacity);
    pooledHeapBuffer1.release();
    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(4 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer4 = ByteBufAllocator.DEFAULT.heapBuffer(1 * 1024 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer5 = ByteBufAllocator.DEFAULT.heapBuffer(3 * 1024 * 1024, maxCapacity);

    System.out.println("testReleaseNormal(): " + (pooledHeapBuffer1 == pooledHeapBuffer3));// true ByteBuf也被复用
    PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer3);
    PrintUtil.print("pooledHeapBuffer4", pooledHeapBuffer4);
    PrintUtil.print("pooledHeapBuffer5", pooledHeapBuffer5);
  }

  /**
   * PooledByteBufAllocator 中说明了 32kb是 默认的 最大缓存
   * // 32 kb is the default maximum capacity of the cached buffer. Similar to what is explained in
   * // 'Scalable memory allocation using jemalloc'
   * <p>
   * 测试回收的内存被 PoolThreadCache 缓存
   */
  @Test
  public void testThreadCache() {
    int maxCapacity = 10 * 1024 * 1024; // 10m

    ByteBuf pooledHeapBuffer1 = ByteBufAllocator.DEFAULT.heapBuffer(7 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer2 = ByteBufAllocator.DEFAULT.heapBuffer(15 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer3 = ByteBufAllocator.DEFAULT.heapBuffer(31 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer4 = ByteBufAllocator.DEFAULT.heapBuffer(63 * 1024, maxCapacity);
    pooledHeapBuffer1.release();// 未释放，被缓存下来
    pooledHeapBuffer2.release();// 未释放，被缓存下来
    pooledHeapBuffer3.release();// 未释放，被缓存下来
    pooledHeapBuffer4.release();// 超过了32k，不缓存，直接释放
    ByteBuf pooledHeapBuffer5 = ByteBufAllocator.DEFAULT.heapBuffer(6 * 1024, maxCapacity);// 从缓存中取
    ByteBuf pooledHeapBuffer6 = ByteBufAllocator.DEFAULT.heapBuffer(12 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer7 = ByteBufAllocator.DEFAULT.heapBuffer(28 * 1024, maxCapacity);
    ByteBuf pooledHeapBuffer8 = ByteBufAllocator.DEFAULT.heapBuffer(40 * 1024, maxCapacity);

    PrintUtil.print("pooledHeapBuffer1", pooledHeapBuffer1);
    PrintUtil.print("pooledHeapBuffer2", pooledHeapBuffer2);
    PrintUtil.print("pooledHeapBuffer3", pooledHeapBuffer3);
    PrintUtil.print("pooledHeapBuffer4", pooledHeapBuffer4);
  }
}
