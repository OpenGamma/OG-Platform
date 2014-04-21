/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link ThreadFactory} which allows the naming of threads rather than simple use of a numeric thread factory ID.
 */
public class NamedThreadPoolFactory implements ThreadFactory {

  /**
   * The thread pool name.
   */
  private final String _poolName;
  /**
   * The thread group.
   */
  private final ThreadGroup _group;
  /**
   * The name prefix to use.
   */
  private final String _namePrefix;
  /**
   * The next thread number to assign.
   */
  private final AtomicInteger _nextThreadNumber = new AtomicInteger();
  /**
   * Whether to make the thread a daemon.
   */
  private final boolean _makeDaemon;

  /**
   * Creates an MDC aware, named, cached thread pool.
   *
   * @param poolName the name to be given to the threads used by the pool
   * @param makeDaemon specifies whether to use daemon threads
   * @return a new executor service
   */
  public static ExecutorService newCachedThreadPool(String poolName, boolean makeDaemon) {
    return new MdcAwareThreadPoolExecutor(new NamedThreadPoolFactory(poolName, makeDaemon));
  }

  /**
   * Creates an MDC aware, named, cached thread pool using daemon threads.
   *
   * @param poolName the name to be given to the threads used by the pool
   * @return a new executor service
   */
  public static ExecutorService newCachedThreadPool(String poolName) {
    return newCachedThreadPool(poolName, true);
  }

  /**
   * Creates a factory with a pool name.
   *
   * @param poolName the pool name, not null
   */
  public NamedThreadPoolFactory(String poolName) {
    this(poolName, true);
  }

  /**
   * Creates a factory with a pool name and daemon flag.
   *
   * @param poolName the pool name, not null
   * @param makeDaemon whether to make the thread a daemon
   */
  public NamedThreadPoolFactory(String poolName, boolean makeDaemon) {
    ArgumentChecker.notNull(poolName, "poolName");
    _poolName = poolName;
    _makeDaemon = makeDaemon;
    SecurityManager s = System.getSecurityManager();
    _group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    _namePrefix = _poolName + "-";
  }

  /**
   * Gets the pool name.
   *
   * @return the poolName the pool name, not null
   */
  public String getPoolName() {
    return _poolName;
  }

  /**
   * Gets whether the factory creates daemon threads.
   *
   * @return whether the factory creates daemon threads
   */
  public boolean isDaemon() {
    return _makeDaemon;
  }

  protected Thread createThread(final ThreadGroup group, final Runnable runnable, final String threadName, final int stackSize) {
    return new Thread(group, runnable, threadName, stackSize);
  }

  /**
   * Creates a new thread using the stored details. This creates a thread using the pool name as a prefix.
   *
   * @param runnable the runnable to use, not null
   * @return the created thread, not null
   */
  @Override
  public Thread newThread(Runnable runnable) {
    String threadName = _namePrefix + _nextThreadNumber.incrementAndGet();
    Thread t = createThread(_group, runnable, threadName, 0);
    if (t.isDaemon() != _makeDaemon) {
      t.setDaemon(_makeDaemon);
    }
    return t;
  }

}
