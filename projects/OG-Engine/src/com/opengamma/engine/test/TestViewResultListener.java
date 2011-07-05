/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.google.common.base.Function;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;

/**
 * Provides generic result handing and waiting functionality.
 */
public class TestViewResultListener implements ViewResultListener {

  private final BlockingQueue<Function<ViewResultListener, ?>> _callsReceived = new LinkedBlockingQueue<Function<ViewResultListener, ?>>();
  
  private long _lastResultReceived;
  private long _shortestDelay;
  
  @SuppressWarnings("unchecked")
  public <T> T expectNextCall(Class<T> expectedResultType, long timeoutMillis) throws InterruptedException {
    Object result = _callsReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    if (result == null) {
      throw new OpenGammaRuntimeException("Timed out after " + timeoutMillis + " ms waiting for result");
    }
    if (!expectedResultType.equals(result.getClass())) {
      throw new OpenGammaRuntimeException("Expected next call of type " + expectedResultType + " but was of type " + result.getClass());
    }
    return (T) result;
  }
  
  //-------------------------------------------------------------------------
  public ViewDefinitionCompiledCall getViewDefinitionCompiled(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ViewDefinitionCompiledCall.class, timeoutMillis);
  }
  
  public CycleCompletedCall getCycleCompleted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(CycleCompletedCall.class, timeoutMillis);
  }
  
  public ProcessCompletedCall getProcessCompleted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ProcessCompletedCall.class, timeoutMillis);
  }
  
  public ProcessTerminatedCall getProcessTerminated(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ProcessTerminatedCall.class, timeoutMillis);
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
      throw new AssertionError("Call received after " + (System.currentTimeMillis () - tNow) + "ms, during " + timeoutMillis + "ms wait: " + result);
    }
  }
  
  public void assertViewDefinitionCompiled() {
    assertViewDefinitionCompiled(0);
  }
  
  public void assertViewDefinitionCompiled(long timeoutMillis) {
    assertViewDefinitionCompiled(timeoutMillis, null);
  }
  
  public void assertViewDefinitionCompiled(long timeoutMillis, CompiledViewDefinition expectedCompiledViewDefinition) {
    ViewDefinitionCompiledCall call;
    try {
      call = getViewDefinitionCompiled(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected viewDefinitionCompiled call error: " + e.getMessage());
    }
    if (expectedCompiledViewDefinition != null) {
      assertEquals(expectedCompiledViewDefinition, ((ViewDefinitionCompiledCall) call).getCompiledViewDefinition());
    }
  }
  
  public void assertCycleCompleted() {
    assertCycleCompleted(0);
  }
  
  public void assertCycleCompleted(long timeoutMillis) {
    assertCycleCompleted(timeoutMillis, null, null);
  }
  
  public void assertCycleCompleted(long timeoutMillis, ViewComputationResultModel expectedFullResult, ViewDeltaResultModel expectedDeltaResult) {
    CycleCompletedCall call;
    try {
      call = getCycleCompleted(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected cycleCompleted call error: " + e.getMessage());
    }
    if (expectedFullResult != null) {
      assertEquals(expectedFullResult, call.getFullResult());
    }
    if (expectedDeltaResult != null) {
      assertEquals(expectedDeltaResult, call.getDeltaResult());
    }
  }
  
  public void assertMultipleCycleCompleted(int count) {
    for (int i = 0; i < count; i++) {
      try {
        assertCycleCompleted(0);
      } catch (Exception e) {
        throw new AssertionError("Expecting " + count + " results but no more found after result " + i);
      }
    }
  }
  
  public void assertProcessCompleted() {
    assertProcessCompleted(0);
  }
  
  public void assertProcessCompleted(long timeoutMillis) {
    try {
      getProcessCompleted(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected processCompleted call error: " + e.getMessage());
    }
  }
  
  public void assertProcessTerminated() {
    assertProcessTerminated(0);
  }
  
  public void assertProcessTerminated(long timeoutMillis) {
    try {
      getProcessTerminated(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected processTerminated call error: " + e.getMessage());
    }
  }
  
  //-------------------------------------------------------------------------
  public synchronized long getShortestDelay () {
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
  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getTestUser();
  }
  
  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    callReceived(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    callReceived(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    callReceived(new CycleCompletedCall(fullResult, deltaResult));
    long now = System.currentTimeMillis();
    long delay = now - _lastResultReceived;
    _lastResultReceived = now;
    if (delay < _shortestDelay) {
      _shortestDelay = delay;
    }
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    callReceived(new CycleExecutionFailedCall(executionOptions, exception));
  }

  @Override
  public void processCompleted() {
    callReceived(new ProcessCompletedCall());
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    callReceived(new ProcessTerminatedCall(executionInterrupted));
  }
  
  //-------------------------------------------------------------------------
  private void callReceived(Function<ViewResultListener, ?> result) {
    _callsReceived.add(result);
  }
  
}
