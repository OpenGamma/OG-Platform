/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link RunQueue} implementations
 */
@Test(groups = TestGroup.UNIT)
public class RunQueueTest {

  private ExecutorService _executor;

  @BeforeClass
  public void init() {
    _executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public void done() {
    _executor.shutdownNow();
    _executor = null;
  }

  private ContextRunnable runnable() {
    return new ContextRunnable() {
      @Override
      public boolean tryRun(final GraphBuildingContext context) {
        // No-op
        return true;
      }
    };
  }

  private void testSpeed(final RunQueue queue, final CyclicBarrier barrier, final double[] speed) {
    try {
      int isEmpty = 0, take = 0, add = 0;
      long time = 0;
      for (int j = 0; j < 2; j++) {
        barrier.await();
        final long start = System.nanoTime();
        isEmpty = 0;
        take = 0;
        add = 0;
        do {
          isEmpty++;
          if (!queue.isEmpty()) {
            take++;
            final ContextRunnable runnable = queue.take();
            if (runnable != null) {
              add++;
              queue.add(runnable);
            }
          }
          time = System.nanoTime() - start;
        } while (time < 5000000000L);
      }
      synchronized (speed) {
        speed[0] += (double) isEmpty / ((double) time / 1e9);
        speed[1] += (double) take / ((double) time / 1e9);
        speed[2] += (double) add / ((double) time / 1e9);
      }
      barrier.await();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    } catch (BrokenBarrierException e) {
      throw new OpenGammaRuntimeException("Broken barrier", e);
    }
  }

  @SuppressWarnings("unused")
  private void testSpeed(final RunQueue queue, final int threads) {
    final double[] speed = new double[3];
    final CyclicBarrier barrier = new CyclicBarrier(threads);
    for (int i = 1; i < threads; i++) {
      _executor.submit(new Runnable() {
        @Override
        public void run() {
          testSpeed(queue, barrier, speed);
        }
      });
    }
    for (int i = 0; i < 100; i++) {
      queue.add(runnable());
    }
    testSpeed(queue, barrier, speed);
    System.out.println(queue + ", " + threads + ", " + speed[0] + ", " + speed[1] + ", " + speed[2]);
  }

  private void testSpeed(final RunQueueFactory queueFactory) {
    /*testSpeed(queueFactory.createRunQueue(), 1);
    testSpeed(queueFactory.createRunQueue(), 4);
    testSpeed(queueFactory.createRunQueue(), 8);
    testSpeed(queueFactory.createRunQueue(), 16);*/
  }

  private void testLIFO(final RunQueueFactory queueFactory) {
    final RunQueue queue = queueFactory.createRunQueue();
    assertTrue(queue.isEmpty());
    assertEquals(queue.size(), 0);
    final ContextRunnable r1 = runnable();
    final ContextRunnable r2 = runnable();
    final ContextRunnable r3 = runnable();
    queue.add(r1);
    assertFalse(queue.isEmpty());
    queue.add(r2);
    queue.add(r3);
    assertEquals(queue.size(), 3);
    assertSame(queue.take(), r3);
    assertSame(queue.take(), r2);
    assertEquals(queue.size(), 1);
    queue.add(r2);
    queue.add(r3);
    assertEquals(queue.size(), 3);
    assertSame(queue.take(), r3);
    assertSame(queue.take(), r2);
    assertSame(queue.take(), r1);
    assertEquals(queue.size(), 0);
    assertTrue(queue.isEmpty());
  }

  private void testFIFO(final RunQueueFactory queueFactory) {
    final RunQueue queue = queueFactory.createRunQueue();
    assertTrue(queue.isEmpty());
    assertEquals(queue.size(), 0);
    final ContextRunnable r1 = runnable();
    final ContextRunnable r2 = runnable();
    final ContextRunnable r3 = runnable();
    queue.add(r1);
    assertFalse(queue.isEmpty());
    queue.add(r2);
    queue.add(r3);
    assertEquals(queue.size(), 3);
    assertSame(queue.take(), r1);
    assertSame(queue.take(), r2);
    assertEquals(queue.size(), 1);
    queue.add(r1);
    queue.add(r2);
    assertEquals(queue.size(), 3);
    assertSame(queue.take(), r3);
    assertSame(queue.take(), r1);
    assertSame(queue.take(), r2);
    assertEquals(queue.size(), 0);
    assertTrue(queue.isEmpty());
  }

  public void testLinkedListLIFO() {
    testSpeed(RunQueueFactory.getLifoLinkedList());
    testLIFO(RunQueueFactory.getLifoLinkedList());
  }

  public void testLinkedListFIFO() {
    testSpeed(RunQueueFactory.getFifoLinkedList());
    testFIFO(RunQueueFactory.getFifoLinkedList());
  }

  public void testConcurrentLinkedQueue() {
    testSpeed(RunQueueFactory.getConcurrentLinkedQueue());
    testFIFO(RunQueueFactory.getConcurrentLinkedQueue());
  }

  public void testStackRunQueue() {
    testSpeed(RunQueueFactory.getConcurrentStack());
    testLIFO(RunQueueFactory.getConcurrentStack());
  }

  public void testOrderedRunQueue() {
    testSpeed(RunQueueFactory.getOrdered());
    testLIFO(RunQueueFactory.getOrdered());
  }

}
