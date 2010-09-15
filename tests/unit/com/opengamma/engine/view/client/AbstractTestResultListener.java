/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Provides generic result handing and waiting functionality.
 */
public class AbstractTestResultListener<T> {

  private List<T> _resultsReceived = new ArrayList<T>();
  private volatile CountDownLatch _resultLatch;

  protected void resultReceived(T result) {
    _resultsReceived.add(result);
    
    CountDownLatch resultLatch = _resultLatch;
    if (resultLatch != null) {
      resultLatch.countDown();
    }
  }
 
  public void setExpectedResultCount(int count) {
    _resultLatch = new CountDownLatch(count);
  }
  
  public void awaitExpectedResults(long timeoutMillis) throws InterruptedException {
    _resultLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    _resultLatch = null;    
  }
  
  public void awaitExpectedResults() throws InterruptedException {
    awaitExpectedResults(0);
  }
  
  public List<T> popResults() {
    List<T> results = _resultsReceived;
    _resultsReceived = new ArrayList<T>();
    return results;
  }
  
}
