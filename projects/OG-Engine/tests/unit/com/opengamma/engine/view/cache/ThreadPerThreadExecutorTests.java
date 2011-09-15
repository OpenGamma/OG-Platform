package com.opengamma.engine.view.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class ThreadPerThreadExecutorTests {

  @Test
  public void TasksExecuted() throws Exception
  {
    ThreadPerThreadExecutor executor = new ThreadPerThreadExecutor();
    Future<?> action1 = executor.submit(new Runnable() {
      
      @Override
      public void run() {
      }
    });
    Future<?> action2 = executor.submit(new Runnable() {
      
      @Override
      public void run() {
      }
    });
    assertEquals(null, action1.get(100, TimeUnit.MILLISECONDS));
    assertEquals(null, action2.get(100, TimeUnit.MILLISECONDS));
    executor.shutdown();
    assertTrue(executor.awaitTermination(100, TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void tasksExecutedOnSameThread() throws Exception
  {
    ThreadPerThreadExecutor executor = new ThreadPerThreadExecutor();
    Callable<Thread> getThreadCallable = new Callable<Thread>() {

      @Override
      public Thread call() throws Exception {
        return Thread.currentThread();
      }
    };
    Future<?> action1 = executor.submit(getThreadCallable);
    Future<?> action2 = executor.submit(getThreadCallable);
    assertEquals(action2.get(100, TimeUnit.MILLISECONDS), action1.get(100, TimeUnit.MILLISECONDS));
    executor.shutdown();
    assertTrue(executor.awaitTermination(100, TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void tasksExecutedOnDifferentThreadIfQueuedOnDifferentThread() throws Exception
  {
    ThreadPerThreadExecutor executor = new ThreadPerThreadExecutor();
    final Callable<Thread> getThreadCallable = new Callable<Thread>() {

      @Override
      public Thread call() throws Exception {
        return Thread.currentThread();
      }
    };
    Future<?> action1 = executor.submit(getThreadCallable);
    final ExecutorService singleThread = Executors.newSingleThreadExecutor();
    Future<Future<Thread>> action2 = singleThread.submit(new Callable<Future<Thread>>(){

      @Override
      public Future<Thread> call() throws Exception {
        return singleThread.submit(getThreadCallable);
      }});
    
    Future<Thread> innerFuture = action2.get(100, TimeUnit.MILLISECONDS);
    assertNotSame(innerFuture.get(100, TimeUnit.MILLISECONDS), action1.get(100, TimeUnit.MILLISECONDS));
    executor.shutdown();
    singleThread.shutdown();
    assertTrue(executor.awaitTermination(100, TimeUnit.MILLISECONDS));
    assertTrue(singleThread.awaitTermination(100, TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void notShuttingDownDoesntHang() throws Exception
  {
    ThreadPerThreadExecutor executor = new ThreadPerThreadExecutor();
    Callable<Thread> getThreadCallable = new Callable<Thread>() {

      @Override
      public Thread call() throws Exception {
        return Thread.currentThread();
      }
    };
    Future<?> action1 = executor.submit(getThreadCallable);
    action1.get(100, TimeUnit.MILLISECONDS);
  }
  
  @Test
  public void threadTimesOut() throws Exception
  {
    ThreadPerThreadExecutor executor = new ThreadPerThreadExecutor();
    Callable<Thread> getThreadCallable = new Callable<Thread>() {

      @Override
      public Thread call() throws Exception {
        return Thread.currentThread();
      }
    };
    Future<?> action1 = executor.submit(getThreadCallable);
    Object result1 = action1.get(100, TimeUnit.MILLISECONDS);
    Thread.sleep(executor.getKeepAliveTimeMillis() * 2);
    Future<?> action2 = executor.submit(getThreadCallable);
    Object result2 = action2.get(100, TimeUnit.MILLISECONDS);
    assertNotSame(result2, result1);
    executor.shutdown();
    assertTrue(executor.awaitTermination(100, TimeUnit.MILLISECONDS));
  }
}
