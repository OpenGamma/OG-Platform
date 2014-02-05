/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Utility methods to simplify shutdown.
 * <p>
 * This is a static thread-safe utility class.
 */
public final class ShutdownUtils {

  /**
   * Hidden constructor.
   */
  private ShutdownUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Exits the JVM, trying to do it nicely, otherwise doing it nastily.
   * <p>
   * A 2 second delay as used before killing nastily.
   * 
   * @param status  the exit status, zero for OK, non-zero for error
   */
  public static void exit(final int status) {
    exit(status, 2000L);
  }

  /**
   * Exits the JVM, trying to do it nicely, otherwise doing it nastily.
   * 
   * @param status  the exit status, zero for OK, non-zero for error
   * @param maxDelayMillis  the maximum delay in milliseconds, zero or negative converted to 2 seconds
   */
  public static void exit(final int status, long maxDelayMillis) {
    if (maxDelayMillis <= 0) {
      maxDelayMillis = 2000L;
    }
    try {
      // setup a timer, so if nice exit fails, the nasty exit happens
      Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          Runtime.getRuntime().halt(status);
        }
      }, maxDelayMillis);
      // try to exit nicely
      System.exit(status);
      
    } catch (Throwable ex) {
      // exit nastily if we have a problem
      Runtime.getRuntime().halt(status);
    } finally {
      // should never get here
      Runtime.getRuntime().halt(status);
    }
  }

}
