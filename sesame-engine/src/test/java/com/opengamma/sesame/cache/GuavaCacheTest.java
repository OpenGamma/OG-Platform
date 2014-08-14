/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opengamma.util.test.TestGroup;

/**
 * Test that checks the behaviour of the Guava cache {@code get()} method.
 * <p>
 * We're relying on the fact that a call to {@code get()} will block if a value is already being
 * calculated for the specified key. It's common sense behaviour, but it isn't explicitly documented.
 * <p>
 * If this test fails it's probably because Guava has been updated and the behaviour has changed (which
 * seems highly unlikely).
 */
@Test(groups = TestGroup.UNIT)
public class GuavaCacheTest {

  private static final Logger s_logger = LoggerFactory.getLogger(GuavaCacheTest.class);

  @Test
  public void getMethodBlocksDuringCalculation() throws ExecutionException {
    final Cache<String, Integer> cache = CacheBuilder.newBuilder().concurrencyLevel(4).maximumSize(50).build();
    final CalculationTask task = new CalculationTask();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          s_logger.info("getting value on other thread");
          cache.get("a", task);
          s_logger.info("got value on other thread");
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }, "other").start();

    s_logger.info("getting value on main thread");
    cache.get("a", task);
    s_logger.info("got value on main thread");

    assertEquals(1, task.getCalculationCount());
  }

  private static class CalculationTask implements Callable<Integer> {

    private AtomicInteger _executionCount = new AtomicInteger(0);

    @Override
    public Integer call() throws Exception {
      s_logger.info("calculating value on thread " + Thread.currentThread());
      _executionCount.getAndIncrement();
      Thread.sleep(3000);
      return 0;
    }

    private int getCalculationCount() {
      return _executionCount.get();
    }
  }
}
