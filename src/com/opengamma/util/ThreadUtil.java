/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utility methods for working with threads.
 *
 * @author kirk
 */
public final class ThreadUtil {
  private ThreadUtil() {
  }
  
  /**
   * Attempt to join the thread specified safely.
   *  
   * @param t The thread to join.
   * @return {@code true} if the join succeeded, or {@code false} if
   *         we went past the timeout.
   */
  public static boolean safeJoin(Thread t, long msTimeout) {
    try {
      t.join(msTimeout);
    } catch (InterruptedException e) {
      // Clear the interrupted state.
      Thread.interrupted();
    }
    return !t.isAlive();
  }

}
