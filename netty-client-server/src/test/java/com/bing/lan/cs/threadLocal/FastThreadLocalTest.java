package com.bing.lan.cs.threadLocal;

import org.junit.Test;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;

/**
 * Created by backend.
 */

public class FastThreadLocalTest {

  private static final FastThreadLocal<String> THREAD_NAME_LOCAL_FAST = new FastThreadLocal<>();
  private static final FastThreadLocal<String> NAME_LOCAL_FAST = new FastThreadLocal<>();

  @Test
  public void testFastThreadLocal() {
    for (int i = 0; i < 2; i++) {
      int index = i;
      String threadName = "thread-" + i;
      new FastThreadLocalThread(() -> {

        THREAD_NAME_LOCAL_FAST.set(threadName);
        String name = index % 2 == 0 ? "aaa" : "bbb";
        NAME_LOCAL_FAST.set(name);
        System.out.println("thread name: " + THREAD_NAME_LOCAL_FAST.get() + ", name info：" + NAME_LOCAL_FAST.get());
      }, threadName).start();
    }

    try {
      Thread.sleep(1000000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("testRecycler(): ");
  }
}
