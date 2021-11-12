package com.bing.lan.cs.objectPool;

import io.netty.util.Recycler;

public class UserCache {

  private static final Recycler<User> RECYCLER = new Recycler<>() {

    @Override
    protected User newObject(Handle<User> handle) {
      return new User(handle);
    }
  };

  public static User newUser() {
    return RECYCLER.get();
  }

  static final class User {

    private String name;
    private Recycler.Handle<User> handle;

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public User(Recycler.Handle<User> handle) {
      this.handle = handle;
    }

    public void recycle() {
      handle.recycle(this);
    }
  }
}
