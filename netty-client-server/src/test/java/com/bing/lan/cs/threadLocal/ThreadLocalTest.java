package com.bing.lan.cs.threadLocal;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * 面试题 - ThreadLocal详解
 * https://blog.csdn.net/wangnanwlw/article/details/108866086
 */

public class ThreadLocalTest {

  private static final ThreadLocal<String> THREAD_NAME_LOCAL = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
  private static final ThreadLocal<String> NAME_LOCAL = new ThreadLocal<>();

  @Test
  public void testThreadLocal() {
    for (int i = 0; i < 2; i++) {
      int index = i;
      String threadName = "thread-" + i;
      new Thread(() -> {

        String name = index % 2 == 0 ? "aaa" : "bbb";
        NAME_LOCAL.set(name);
        System.out.println("thread name: " + THREAD_NAME_LOCAL.get() + ", name info：" + NAME_LOCAL.get());
      }, threadName).start();
    }

    try {
      Thread.sleep(1000000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("testRecycler(): ");
  }

  @Test
  public void testThreadLocalHashCode() {
    printAllSlot(8);
    printAllSlot(16);
    printAllSlot(32);
  }

  private static void printAllSlot(int len) {
    System.out.println("********** len = " + len + " ************");
    for (int i = 1; i <= 64; i++) {
      ThreadLocal<String> t = new ThreadLocal<>();
      int slot = getSlot(t, len);
      System.out.print(slot + " ");
      if (i % len == 0) {
        System.out.println(); // 分组换行
      }
    }
  }

  /**
   * 获取槽位
   *
   * @param t   ThreadLocal
   * @param len 模拟map的table的length
   */
  private static int getSlot(ThreadLocal<?> t, int len) {
    int hash = getHashCode(t);
    return hash & (len - 1);
  }

  /**
   * 反射获取 threadLocalHashCode 字段，因为其为private的
   */
  private static int getHashCode(ThreadLocal<?> t) {
    Field field;
    try {
      field = t.getClass().getDeclaredField("threadLocalHashCode");
      field.setAccessible(true);
      return (int) field.get(t);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Test
  public void testThreadLocalWeakReference() {
    Thread thread = Thread.currentThread();
    ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
    threadLocal.set(1);
    threadLocal = null;
    System.gc();// JVM触发GC回收弱引用
    // Entry中的key变量：referent 已经被回收, 但是Entry中的value是强引用，还存放着threadLocal未回收之前的值，需要惰性删除
    System.out.println("testThreadLocalWeakReference(): " + thread.getName());
  }
}

