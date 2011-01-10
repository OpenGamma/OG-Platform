/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.concurrent.CountDownLatch;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Reports a "standard" timeout duration based on a performance benchmark. Tests
 * requiring a timeout should use an appropriate multiple of this so that they
 * behave comparably on systems with different performances.
 */
public final class Timeout {

  private static final int NUM_THREADS = 8;
  private static final int NUM_CYCLES = 10000;
  private static final int ALLOC_LENGTH = 16384;

  private static long s_nanoBenchmark;

  private Timeout() {
  }

  /**
   * Run the benchmark. It consists of thread creation, allocating
   * memory, some CPU cycles, and barrier completion of the threads.
   */
  static {
    long b = System.nanoTime();
    final CountDownLatch barrier = new CountDownLatch(NUM_THREADS);
    for (int i = 0; i < NUM_THREADS; i++) {
      final Thread slave = new Thread() {
        @Override
        public void run() {
          for (int i = 0; i < NUM_CYCLES; i++) {
            final double[] block = new double[ALLOC_LENGTH];
            block[0] = 1.0;
            for (int j = 1; j < block.length; j++) {
              block[j] = block[j - 1] * 1.001;
            }
          }
          barrier.countDown();
        }
      };
      if (i < NUM_THREADS - 1) {
        slave.start();
      } else {
        slave.run();
      }
    }
    try {
      barrier.await();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    s_nanoBenchmark = System.nanoTime() - b;
    // System.out.println("Standard benchmark time = " + s_nanoBenchmark + "ns");
  }

  public static long standardTimeoutMillis() {
    return (s_nanoBenchmark / 1000000L) + 1L;
  }

  public static long standardTimeoutNanos() {
    return s_nanoBenchmark + 1L;
  }

  public static long standardTimeoutSeconds() {
    return (s_nanoBenchmark / 1000000000L) + 1L;
  }

}
