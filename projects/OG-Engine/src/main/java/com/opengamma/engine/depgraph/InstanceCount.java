/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Utility class for profiling the number of active objects. For development/debug use only.
 */
/* package */final class InstanceCount {

  private static final ConcurrentMap<Class<?>, AtomicInteger> s_instanceCount = new ConcurrentHashMap<Class<?>, AtomicInteger>();

  private final AtomicInteger _count;

  static {
    new Thread(new Runnable() {
      @Override
      public void run() {
        do {
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("interrupted", e);
          }
          for (Map.Entry<Class<?>, AtomicInteger> instance : s_instanceCount.entrySet()) {
            System.out.println(instance.getKey() + "\t" + instance.getValue());
          }
        } while (true);
      }
    }).start();
  }

  public InstanceCount(final Object owner) {
    AtomicInteger count = s_instanceCount.get(owner.getClass());
    if (count == null) {
      count = new AtomicInteger(1);
      final AtomicInteger existing = s_instanceCount.putIfAbsent(owner.getClass(), count);
      if (existing != null) {
        existing.incrementAndGet();
        count = existing;
      }
    } else {
      count.incrementAndGet();
    }
    _count = count;
  }

  protected void finalize() throws Throwable {
    super.finalize();
    _count.decrementAndGet();
  }

}
