/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.MDC;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;


/**
 * Tests that MdcAwareThreadPoolExecutorTest correctly segregates MDC
 * information from different threads. Prints output to standard out
 * as it's otherwise difficult to check that everything is created by
 * the expected thread.
 */
@Test(groups = TestGroup.UNIT)
public class MdcAwareThreadPoolExecutorTest {

  private ExecutorService createMdcAwareService(int threads) {
    return new MdcAwareThreadPoolExecutor(threads,
                                              threads,
                                              60,
                                              TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>(),
                                              new NamedThreadPoolFactory("mdcAware"));
  }

  private ExecutorService createStandardService(int threads) {
    return new ThreadPoolExecutor(threads,
                                              threads,
                                              60,
                                              TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>(),
                                              new NamedThreadPoolFactory("standard"));
  }

  @Test(enabled = false)
  public void testMdcIdLostWhenUsingStandardPool() throws InterruptedException {

    // This is the case we want our new code to cope with. i.e. a thread pool losing
    // the MDC information. Unfortunately, the test below fails as the MDC data
    // *is* being magically passed across the threads
    final ExecutorService service = createStandardService(3);
    final AtomicInteger checkCount = new AtomicInteger(0);
    createStarterThreads(3, service, checkCount, false);

    // Wait for the threads to complete
    Thread.sleep(200);

    assertThat(checkCount.get(), is(3));
  }

  @Test
  public void testMdcIdLoggedForSingleThread() throws InterruptedException {

    final ExecutorService service = createMdcAwareService(1);
    final AtomicInteger checkCount = new AtomicInteger(0);
    createStarterThreads(1, service, checkCount, true);

    Thread.sleep(200);

    assertThat(checkCount.get(), is(1));
  }

  @Test
  public void testCorrectIdLoggedForMultipleThreads() throws InterruptedException {

    final ExecutorService service = createMdcAwareService(3);
    final AtomicInteger checkCount = new AtomicInteger(0);
    createStarterThreads(3, service, checkCount, true);

    Thread.sleep(200);

    assertThat(checkCount.get(), is(3));
  }

  @Test
  public void testCorrectIdLoggedForMultipleThreadsWithPoolReuse() throws InterruptedException {

    final ExecutorService service = createMdcAwareService(3);
    final AtomicInteger checkCount = new AtomicInteger(0);

    createStarterThreads(3, service, checkCount, true);
    Thread.sleep(200);

    createStarterThreads(3, service, checkCount, true);
    Thread.sleep(200);

    createStarterThreads(3, service, checkCount, true);
    Thread.sleep(200);

    createStarterThreads(3, service, checkCount, true);
    Thread.sleep(200);

    assertThat(checkCount.get(), is(12));
  }

  private void createStarterThreads(int qty,
                                    final ExecutorService service,
                                    final AtomicInteger checkCount,
                                    final boolean mdcPropagationExpected) {

    Set<Thread> threads = new HashSet<>();
    for (int i = 0; i < qty; i++) {

      final int value = i;
      threads.add(new Thread(new Runnable() {
        @Override
        public void run() {
          Map<String, String> contextMap = new HashMap<>();
          contextMap.put("some_key" + value, "somevalue");
          contextMap.put("name", Thread.currentThread().getName());
          System.out.println("Putting MDC values: " + contextMap);
          MDC.setContextMap(contextMap);
          Map<String, String> expectedContext = mdcPropagationExpected ? contextMap : null;
          service.execute(createCheckJob(expectedContext, checkCount));
        }
      }, "starter" + i));
    }
    System.out.println("Threads created - MDC contains: " + MDC.getCopyOfContextMap());
    for (Thread thread : threads) {
      thread.start();
    }
    System.out.println("Threads started - MDC contains: " + MDC.getCopyOfContextMap());
  }

  private Runnable createCheckJob(final Map<String, String> expectedContext, final AtomicInteger checkCount) {

    System.out.println("Creating check job on thread - " +  Thread.currentThread().getName() + " MDC: " + MDC.getCopyOfContextMap());
    return new Runnable() {
      @Override
      public void run() {
        @SuppressWarnings("unchecked")
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        System.out.println("Running on thread-" + Thread.currentThread().getName() + " => " + mdc) ;
        try {
          assertThat(mdc, is(expectedContext));
          checkCount.incrementAndGet();
        } catch (Throwable e) {
          System.out.println(e);
        }
      }
    };
  }
}
