/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.spring;

import org.springframework.context.Lifecycle;

/**
 * Helper methods for objects that might implement the {@link Lifecycle} interface.
 */
public final class LifecycleUtils {

  /**
   * Prevents construction.
   */
  private LifecycleUtils() {
  }

  /**
   * Calls the {@link Lifecycle#start} method if available on the object.
   * 
   * @param o object to test and call
   */
  public static void start(final Object o) {
    if (o instanceof Lifecycle) {
      ((Lifecycle) o).start();
    }
  }

  /**
   * Calls the {@link Lifecycle#isRunning} method if available on the object, returning a default state if it's not implemented
   * 
   * @param o object to test and call
   * @param defaultState the default return value if the object doesn't implement {@link Lifecycle}
   * @return the running state
   */
  public static boolean isRunning(final Object o, final boolean defaultState) {
    if (o instanceof Lifecycle) {
      return ((Lifecycle) o).isRunning();
    } else {
      return defaultState;
    }
  }

  /**
   * Calls the {@link Lifecycle#stop} method if available on the object.
   * 
   * @param o object to test and call
   */
  public static void stop(final Object o) {
    if (o instanceof Lifecycle) {
      ((Lifecycle) o).stop();
    }
  }

}
