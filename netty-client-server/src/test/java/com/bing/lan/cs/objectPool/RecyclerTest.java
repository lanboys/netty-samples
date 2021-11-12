package com.bing.lan.cs.objectPool;

import org.junit.Test;

public class RecyclerTest {

  @Test
  public void testRecycler() {
    UserCache.User user1 = UserCache.newUser(); // 1、从对象池获取 User 对象
    user1.setName("hello"); // 2、设置 User 对象的属性
    user1.recycle(); // 3、回收对象到对象池

    UserCache.User user2 = UserCache.newUser(); // 4、从对象池获取对象

    System.out.println(user2.getName());
    System.out.println(user1 == user2);
  }

  @Test
  public void testRecyclerId() {
    UserCache.User user1 = UserCache.newUser();
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        //user1.setName("hello");
        //user1.recycle();
        //System.out.println("run(): ");
      }
    });
    t1.start();

    //try {
    //  Thread.sleep(100000);
    //} catch (InterruptedException e) {
    //  e.printStackTrace();
    //}

    UserCache.User user2 = UserCache.newUser();
    user2.recycle();

    UserCache.User user3 = UserCache.newUser();
    UserCache.User user4 = UserCache.newUser();

    user3.recycle();
    user4.recycle();

    try {
      t1.join();
      UserCache.User user5 = UserCache.newUser();
      System.out.println("testRecyclerId(): ");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
