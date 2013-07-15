/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode;

import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.JobInvocationReceiver;
import com.opengamma.engine.calcnode.JobInvoker;

/**
 * Simple JobInvocationReceiver for use in the unit tests. Stores the result and allows the caller
 * to block until a result is written.
 */
public class TestJobInvocationReceiver implements JobInvocationReceiver {

  private CalculationJobResult _completionResult;
  private Exception _failureResult;

  @Override
  public synchronized void jobCompleted(final CalculationJobResult result) {
    _completionResult = result;
    notify();
  }

  @Override
  public synchronized void jobFailed(final JobInvoker jobInvoker, final String nodeId, final Exception failure) {
    _failureResult = failure;
    notify();
  }

  public CalculationJobResult getCompletionResult() {
    return _completionResult;
  }
  
  public Exception getFailureResult () {
    return _failureResult;
  }
  
  private synchronized void waitForResult (final long timeoutMillis) {
    if ((_completionResult == null) && (_failureResult == null)) {
      try {
        wait(timeoutMillis);
      } catch (InterruptedException e) {
      }
    }
  }

  public CalculationJobResult waitForCompletionResult(final long timeoutMillis) {
    waitForResult (timeoutMillis);
    return _completionResult;
  }
  
  public Exception waitForFailureResult (final long timeoutMillis) {
    waitForResult (timeoutMillis);
    return _failureResult;
  }

}
