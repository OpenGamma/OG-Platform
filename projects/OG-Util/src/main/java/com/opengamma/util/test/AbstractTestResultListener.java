/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Abstract base class for use by test listeners. Implementations should register any calls received through the
 * {@code callReceived} methods.
 * <p>
 * Generic result handing and waiting functionality is provided for use in test cases.
 */
public abstract class AbstractTestResultListener {

  private final BlockingQueue<Object> _callsReceived = new LinkedBlockingQueue<Object>();
  
  private long _lastResultReceived;
  private long _shortestDelay;
  
  @SuppressWarnings("unchecked")
  public <T> T expectNextCall(Class<T> expectedResultType, long timeoutMillis) throws InterruptedException {
    Object result = _callsReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    if (result == null) {
      throw new OpenGammaRuntimeException("Timed out after " + timeoutMillis + " ms waiting for result");
    }
    if (!expectedResultType.equals(result.getClass())) {
      throw new OpenGammaRuntimeException("Expected next call of type " + expectedResultType + " but was of type " + result.getClass() + ": " + result);
    }
    return (T) result;
  }
  
  //-------------------------------------------------------------------------
  public void assertNoCalls() {
    assertNoCalls(0);
  }
  
  public void assertNoCalls(long timeoutMillis) {
    long tNow = System.currentTimeMillis();
    Object result;
    try {
      result = _callsReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new AssertionError("Error while waiting to ensure no further calls: " + e.getMessage()); 
    }
    if (result != null) {
      throw new AssertionError("Call received after " + (System.currentTimeMillis() - tNow) + "ms, during " + timeoutMillis + "ms wait: " + result);
    }
  }

  //-------------------------------------------------------------------------
  public synchronized long getShortestDelay() {
    return _shortestDelay;
  }

  public void resetShortestDelay() {
    _shortestDelay = Long.MAX_VALUE;
  }

  public int getQueueSize() {
    return _callsReceived.size();
  }

  public void clear() {
    _callsReceived.clear();
  }

  //-------------------------------------------------------------------------
  protected void callReceived(Object call) {
    callReceived(call, false);
  }

  protected void callReceived(Object call, boolean recordTime) {
    _callsReceived.add(call);
    long now = System.currentTimeMillis();
    long delay = now - _lastResultReceived;
    _lastResultReceived = now;
    if (delay < _shortestDelay) {
      _shortestDelay = delay;
    }
  }

}
