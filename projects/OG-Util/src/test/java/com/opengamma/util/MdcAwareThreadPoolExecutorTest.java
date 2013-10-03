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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;


/**
 * Tests that MdcAwareThreadPoolExecutorTest correctly segregates MDC
 * information from different threads.
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

  private static ExecutorService createStandardService(int threads) {
    return new ThreadPoolExecutor(threads,
                                              threads,
                                              60,
                                              TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>(),
                                              new NamedThreadPoolFactory("standard"));
  }

  private Runnable createCheckJob(final Map expectedContext, final AtomicInteger checkCount) {

    System.out.println("Creating check job on thread - " +  Thread.currentThread().getName() + " MDC: " + MDC.getCopyOfContextMap());
    return new Runnable() {
      @Override
      public void run() {

        System.out.println("Running on thread-" + Thread.currentThread().getName() + " == " + MDC.getCopyOfContextMap()) ;
        try {
          assertThat(MDC.getCopyOfContextMap(), is(expectedContext));
          checkCount.incrementAndGet();
        } catch (Throwable e) {
          System.out.println(e);
        }
      }
    };
  }

  public void testMdcIdLostWhenUsingStandardPool() throws InterruptedException {
    final ExecutorService service = createStandardService(3);
    final AtomicInteger checkCount = new AtomicInteger(0);
    //new Thread(new Runnable() {
    //  @Override
    //  public void run() {
    //    MDC.put("some_key", "somevalue");
    //    MDC.put("name", Thread.currentThread().getName());
    //    s_service.execute(createCheckJob(null, checkCount));
    //  }
    //}, "starter").start();

    createStarterThreads(3, service, checkCount);


    Thread.sleep(1000);

    assertThat(checkCount.get(), is(3));
  }

  public void testMdcIdLoggedForSingleThread() throws InterruptedException {

    final ExecutorService service = createMdcAwareService(1);
    final AtomicInteger checkCount = new AtomicInteger(0);
    createStarterThreads(1, service, checkCount);

    Thread.sleep(1000);

    assertThat(checkCount.get(), is(1));
  }

  public void testCorrectIdLoggedForMultipleThreads() throws InterruptedException {

    final ExecutorService service = createMdcAwareService(3);
    final AtomicInteger checkCount = new AtomicInteger(0);
    createStarterThreads(3, service, checkCount);

    Thread.sleep(1000);

    assertThat(checkCount.get(), is(3));
  }

  private void createStarterThreads(int qty, final ExecutorService service, final AtomicInteger checkCount) {

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
          service.execute(createCheckJob(contextMap, checkCount));
        }
      }, "starter" + i));
    }
    System.out.println("Threads created");
    for (Thread thread : threads) {
      thread.start();
    }
    System.out.println("Threads started");
  }

// setup mdc aware executors pool
  // start pair of threads with different MDC contexts

  // execute simultaneous jobs using the executors pool and verify MDC information for each is as expected


}
