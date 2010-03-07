/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link ThreadFactory} which allows the naming of
 * threads rather than simple use of a numeric thread factory ID.
 * Code largely taken from {@link Executors.DefaultThreadFactory}.
 *
 * @author kirk
 */
public class NamedThreadPoolFactory implements ThreadFactory {
  public static final boolean DEFAULT_MAKE_DAEMON = true;
  private final String _poolName;
  private final ThreadGroup _group;
  private final String _namePrefix;
  private final AtomicInteger _nextThreadNumber = new AtomicInteger(1);
  private final boolean _makeDaemon;
  
  public NamedThreadPoolFactory(String poolName) {
    this(poolName, DEFAULT_MAKE_DAEMON);
  }
  
  public NamedThreadPoolFactory(String poolName, boolean makeDaemon) {
    ArgumentChecker.checkNotNull(poolName, "Pool name");
    _poolName = poolName;
    _makeDaemon = makeDaemon;
    
    SecurityManager s = System.getSecurityManager();
    _group = (s != null)? s.getThreadGroup() :
                         Thread.currentThread().getThreadGroup();
    _namePrefix = _poolName + "-";
  }

  /**
   * @return the poolName
   */
  public String getPoolName() {
    return _poolName;
  }

  @Override
  public Thread newThread(Runnable r) {
    String threadName = _namePrefix + _nextThreadNumber.getAndIncrement();
    Thread t = new Thread(_group, r,
        threadName,
        0);
    if(t.isDaemon() != _makeDaemon) {
      t.setDaemon(_makeDaemon);
    }
    return t;
  }

}
