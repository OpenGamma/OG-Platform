/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utility methods for working with threads.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ThreadUtils {

  /**
   * Restricted constructor.
   */
  private ThreadUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Attempt to join the thread specified safely.
   *  
   * @param thread  the thread to join, not null
   * @param timeoutMillis  the timeout in milliseconds
   * @return true if the join succeeded, false if a timeout occurred
   */
  public static boolean safeJoin(Thread thread, long timeoutMillis) {
    if (!thread.isAlive()) {
      return true;
    }
    try {
      thread.join(timeoutMillis);
    } catch (InterruptedException e) {
      // clear the interrupted state
      Thread.interrupted();
    }
    return !thread.isAlive();
  }

}
