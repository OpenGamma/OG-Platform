/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode;

import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.JobResultReceiver;

/**
 * Simple JobResultReceiver for use in the unit tests. Stores the result and allows the caller
 * to block until a result is written.
 */
public class TestJobResultReceiver implements JobResultReceiver {
    
  private CalculationJobResult _result;

  @Override
  public synchronized void resultReceived(final CalculationJobResult result) {
    _result = result;
    notify ();
  }
  
  public CalculationJobResult getResult () {
    return _result;
  }
  
  public synchronized CalculationJobResult waitForResult (final long timeoutMillis) {
    if (_result == null) {
      try {
        wait (timeoutMillis);
      } catch (InterruptedException e) {
        return null;
      }
    }
    return _result;
  }
    
}
