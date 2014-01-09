/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ClientShutdownCall;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.AbstractTestResultListener;

/**
 * Implementation of {@link ViewResultListener} for use in tests.
 */
public class TestViewResultListener extends AbstractTestResultListener implements ViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(TestViewResultListener.class);

  public ViewDefinitionCompiledCall getViewDefinitionCompiled(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ViewDefinitionCompiledCall.class, timeoutMillis);
  }

  public ViewDefinitionCompilationFailedCall getViewDefinitionCompilationFailed(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ViewDefinitionCompilationFailedCall.class, timeoutMillis);
  }

  public CycleStartedCall getCycleStarted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(CycleStartedCall.class, timeoutMillis);
  }

  public CycleCompletedCall getCycleCompleted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(CycleCompletedCall.class, timeoutMillis);
  }

  public CycleFragmentCompletedCall getCycleFragmentCompleted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(CycleFragmentCompletedCall.class, timeoutMillis);
  }

  public CycleExecutionFailedCall getCycleExecutionFailed(long timeoutMillis) throws InterruptedException {
    return expectNextCall(CycleExecutionFailedCall.class, timeoutMillis);
  }

  public ProcessCompletedCall getProcessCompleted(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ProcessCompletedCall.class, timeoutMillis);
  }

  public ProcessTerminatedCall getProcessTerminated(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ProcessTerminatedCall.class, timeoutMillis);
  }

  public ClientShutdownCall getClientShutdown(long timeoutMillis) throws InterruptedException {
    return expectNextCall(ClientShutdownCall.class, timeoutMillis);
  }

  //-------------------------------------------------------------------------  
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

  public void assertViewDefinitionCompilationFailed() {
    assertViewDefinitionCompilationFailed(0);
  }

  public void assertViewDefinitionCompilationFailed(long timeoutMillis) {
    assertViewDefinitionCompilationFailed(timeoutMillis, null);
  }

  public void assertViewDefinitionCompilationFailed(long timeoutMillis, String exceptionMessage) {
    ViewDefinitionCompilationFailedCall call;
    try {
      call = getViewDefinitionCompilationFailed(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected viewDefinitionCompilationFailed call error: " + e.getMessage());
    }
    if (exceptionMessage != null) {
      assertEquals(exceptionMessage, call.getException().getMessage());
    }
  }

  public void assertCycleStarted() {
    assertCycleStarted(0);
  }

  public void assertCycleStarted(long timeoutMillis) {
    try {
      getCycleStarted(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected cycleStarted call error: " + e.getMessage());
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

  public void assertCycleFragmentCompleted() {
    assertCycleFragmentCompleted(0);
  }

  public void assertCycleFragmentCompleted(long timeoutMillis) {
    assertCycleFragmentCompleted(timeoutMillis, null, null);
  }

  public void assertCycleFragmentCompleted(long timeoutMillis, ViewResultModel expectedFullResult, ViewDeltaResultModel expectedDeltaResult) {
    CycleFragmentCompletedCall call;
    try {
      call = getCycleFragmentCompleted(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected cycleFragmentCompleted call error: " + e.getMessage());
    }
    if (expectedFullResult != null) {
      assertEquals(expectedFullResult, call.getFullFragment());
    }
    if (expectedDeltaResult != null) {
      assertEquals(expectedDeltaResult, call.getDeltaFragment());
    }
  }

  public void assertCycleFragmentCompleted(int count) {
    for (int i = 0; i < count; i++) {
      try {
        assertCycleFragmentCompleted(0);
      } catch (Exception e) {
        throw new AssertionError("Expecting " + count + " results but no more found after result " + i);
      }
    }
  }

  public void assertCycleExecutionFailed() {
    assertCycleExecutionFailed(0);
  }

  public void assertCycleExecutionFailed(long timeoutMillis) {
    try {
      getCycleExecutionFailed(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected cycleExecutionFailed call error: " + e.getMessage());
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

  public void assertClientShutdown() {
    assertClientShutdown(0);
  }

  public void assertClientShutdown(long timeoutMillis) {
    try {
      getClientShutdown(timeoutMillis);
    } catch (Exception e) {
      throw new AssertionError("Expected clientShutdown call error: " + e.getMessage());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getTestUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    s_logger.debug("viewDefinitionCompiled ({}, {})", compiledViewDefinition, hasMarketDataPermissions);
    callReceived(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    s_logger.debug("viewDefinitionCompilationFailed ({}, {})", valuationTime, exception);
    callReceived(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
  }

  @Override
  public void cycleStarted(ViewCycleMetadata cycleMetadata) {
    s_logger.debug("cycleStarted ({})", cycleMetadata);
    callReceived(new CycleStartedCall(cycleMetadata), true);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    s_logger.debug("cycleCompleted ({}, {})", fullResult, deltaResult);
    callReceived(new CycleCompletedCall(fullResult, deltaResult), true);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    s_logger.debug("cycleExecutionFailed ({}, {})", executionOptions, exception);
    callReceived(new CycleExecutionFailedCall(executionOptions, exception));
  }

  @Override
  public void processCompleted() {
    s_logger.debug("processCompleted ()");
    callReceived(new ProcessCompletedCall());
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    s_logger.debug("cycleFragmentCompleted ({}, {})", fullResult, deltaResult);
    callReceived(new CycleFragmentCompletedCall(fullResult, deltaResult), true);
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    s_logger.debug("processTerminated ({})", executionInterrupted);
    callReceived(new ProcessTerminatedCall(executionInterrupted));
  }

  @Override
  public void clientShutdown(Exception e) {
    s_logger.debug("clientShutdown ()", e);
    callReceived(new ClientShutdownCall(e));
  }

}
