/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Provides generic result handing and waiting functionality.
 */
public class AbstractTestResultListener<T> {

  private BlockingQueue<T> _resultsReceived = new LinkedBlockingQueue<T>();

  protected void resultReceived(T result) {
    _resultsReceived.add(result);
  }
  
  public T getResult(long timeoutMillis) throws InterruptedException {
    T result = _resultsReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    if (result == null) {
      throw new OpenGammaRuntimeException("Timed out after " + timeoutMillis + " ms waiting for result");
    }
    return result;
  }
  
  public void assertNoResult (long timeoutMillis) throws InterruptedException {
    long tNow = System.currentTimeMillis ();
    T result = _resultsReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    if (result != null) {
      throw new OpenGammaRuntimeException ("Result received after " + (System.currentTimeMillis () - tNow) + "ms, during " + timeoutMillis + "ms wait");
    }
  }
  
  public int getQueueSize() {
    return _resultsReceived.size();
  }
  
  public void clear() {
    _resultsReceived.clear();
  }
  
}
