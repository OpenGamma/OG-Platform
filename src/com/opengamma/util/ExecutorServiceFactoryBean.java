/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Creates an ExecutorService using the {@link Executors} utility methods.
 */
public class ExecutorServiceFactoryBean extends SingletonFactoryBean<ExecutorService> {

  /**
   * Style of executor; corresponds to the names from the {@link Executors} class.
   */
  public static enum Style {
    /**
     * {@link Executors#newCachedThreadPool}.
     */
    CACHED,
    /**
     * {@link Executors#newFixedThreadPool}.
     */
    FIXED,
    /**
     * {@link Executors#newScheduledThreadPool}.
     */
    SCHEDULED,
    /**
     * {@link Executors#newSingleThreadExecutor}.
     */
    SINGLE,
    /**
     * {@link Executors#newSingleScheduledThreadExecutor}.
     */
    SINGLE_SCHEDULED;
  }

  private ThreadFactory _threadFactory;
  private int _numThreads;
  private Style _style;

  public void setThreadFactory(final ThreadFactory threadFactory) {
    _threadFactory = threadFactory;
  }

  public ThreadFactory getThreadFactory() {
    return _threadFactory;
  }

  public void setNumberOfThreads(final int numThreads) {
    ArgumentChecker.notNegativeOrZero(numThreads, "numThreads");
    _numThreads = numThreads;
  }

  public int getNumberOfThreads() {
    return _numThreads;
  }

  public void setStyle(final Style style) {
    _style = style;
  }

  public void setStyleName(final String style) {
    setStyle(Style.valueOf(style));
  }

  public Style getStyle() {
    return _style;
  }

  @Override
  protected ExecutorService createObject() {
    ArgumentChecker.notNull(getStyle(), "style");
    switch (getStyle()) {
      case CACHED:
        if (getThreadFactory() != null) {
          return Executors.newCachedThreadPool(getThreadFactory());
        } else {
          return Executors.newCachedThreadPool();
        }
      case FIXED:
        ArgumentChecker.notNegativeOrZero(getNumberOfThreads(), "numberOfThreads");
        if (getThreadFactory() != null) {
          return Executors.newFixedThreadPool(getNumberOfThreads(), getThreadFactory());
        } else {
          return Executors.newFixedThreadPool(getNumberOfThreads());
        }
      case SCHEDULED:
        ArgumentChecker.notNegativeOrZero(getNumberOfThreads(), "numberOfThreads");
        if (getThreadFactory() != null) {
          return Executors.newScheduledThreadPool(getNumberOfThreads(), getThreadFactory());
        } else {
          return Executors.newScheduledThreadPool(getNumberOfThreads());
        }
      case SINGLE:
        if (getThreadFactory() != null) {
          return Executors.newSingleThreadExecutor(getThreadFactory());
        } else {
          return Executors.newSingleThreadExecutor();
        }
      case SINGLE_SCHEDULED:
        if (getThreadFactory() != null) {
          return Executors.newSingleThreadScheduledExecutor(getThreadFactory());
        } else {
          return Executors.newSingleThreadScheduledExecutor();
        }
      default:
        throw new IllegalStateException("Unhandled executor style - " + getStyle());
    }
  }

}
