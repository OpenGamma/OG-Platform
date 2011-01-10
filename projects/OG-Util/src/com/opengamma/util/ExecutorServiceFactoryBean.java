/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Creates an ExecutorService using the {@link Executors} utility methods.
 * <p>
 * This factory bean aids construction of executor services from Spring.
 */
public class ExecutorServiceFactoryBean extends SingletonFactoryBean<ExecutorService> {

  /**
   * The style of executor required.
   * The names correspond to those in the {@link Executors} class.
   */
  public static enum Style {
    /**
     * Creates using {@link Executors#newCachedThreadPool}.
     */
    CACHED,
    /**
     * Creates using {@link Executors#newFixedThreadPool}.
     */
    FIXED,
    /**
     * Creates using {@link Executors#newScheduledThreadPool}.
     */
    SCHEDULED,
    /**
     * Creates using {@link Executors#newSingleThreadExecutor}.
     */
    SINGLE,
    /**
     * Creates using {@link Executors#newSingleThreadScheduledExecutor}.
     */
    SINGLE_SCHEDULED;
  }

  /**
   * The thread factory.
   */
  private ThreadFactory _threadFactory;
  /**
   * The number of threads.
   */
  private int _numThreads;
  /**
   * The style required.
   */
  private Style _style;

  //-------------------------------------------------------------------------
  /**
   * Gets the thread factory.
   * @return the thread factory
   */
  public ThreadFactory getThreadFactory() {
    return _threadFactory;
  }

  /**
   * Sets the thread factory.
   * @param threadFactory  the thread factory
   */
  public void setThreadFactory(final ThreadFactory threadFactory) {
    _threadFactory = threadFactory;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of threads.
   * @return the thread factory
   */
  public int getNumberOfThreads() {
    return _numThreads;
  }

  /**
   * Sets the number of threads.
   * @param numberOfThreads  the number of threads, 1 or greater
   */
  public void setNumberOfThreads(final int numberOfThreads) {
    ArgumentChecker.notNegativeOrZero(numberOfThreads, "numberOfThreads");
    _numThreads = numberOfThreads;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the required style.
   * @return the required style
   */
  public Style getStyle() {
    return _style;
  }

  /**
   * Sets the required style.
   * @param style  the required style
   */
  public void setStyle(final Style style) {
    _style = style;
  }

  /**
   * Sets the required style.
   * @param style  the required style name
   */
  public void setStyleName(final String style) {
    setStyle(Style.valueOf(style));
  }

  //-------------------------------------------------------------------------
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
