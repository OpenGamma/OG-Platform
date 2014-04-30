/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.env;

/**
 * A thread-local holder for an {@link AnalyticsEnvironment} accessible across the system.
 * @see AnalyticsEnvironment
 */
 /* package */ final class ThreadLocalAnalyticsEnvironment {

  /**
   * The thread-local environment.
   */
  private static ThreadLocal<AnalyticsEnvironment> s_instance = new InheritableThreadLocal<>();

  /**
   * Sets the environment applicable to this thread.
   *
   * @param analyticsEnvironment  the context, may be null
   */
  public static void init(AnalyticsEnvironment analyticsEnvironment) {
    s_instance.set(analyticsEnvironment);
  }

  /**
   * Gets the environment applicable to this thread.
   *
   * @return the analytics environment, null if not initialized
   */
  public static AnalyticsEnvironment getInstance() {
    return s_instance.get();
  }

  /**
   * Restricted constructor.
   */
  private ThreadLocalAnalyticsEnvironment() {
  }

}
