/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.cache.DeferredStatistics;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;

/**
 * 
 */
/* package */class DeferredInvocationStatistics implements DeferredStatistics {

  private final FunctionInvocationStatisticsGatherer _gatherer;
  private final String _configuration;
  private String _functionIdentifier;
  private long _invocationTime;
  private double _dataInputBytes;
  private int _dataOutputBytes;
  private int _dataOutputSamples;
  private int _expectedDataOutputSamples;

  protected DeferredInvocationStatistics(final FunctionInvocationStatisticsGatherer gatherer, final String configuration) {
    _gatherer = gatherer;
    _configuration = configuration;
  }

  protected void beginInvocation(final String functionIdentifier) {
    _invocationTime = System.nanoTime();
    _functionIdentifier = functionIdentifier;
  }

  protected void endInvocation() {
    _invocationTime = System.nanoTime() - _invocationTime;
  }

  protected void setDataInputBytes(final int bytes, final int samples) {
    if (samples > 0) {
      _dataInputBytes = (double) bytes / (double) samples;
    } else {
      _dataInputBytes = Double.NaN;
    }
  }

  protected void setExpectedDataOutputSamples(final int samples) {
    _expectedDataOutputSamples = samples;
  }

  @Override
  public void reportEstimatedSize(final ComputedValue value, final Integer bytes) {
    if (bytes != null) {
      _dataOutputBytes += bytes;
      _dataOutputSamples++;
    }
    _expectedDataOutputSamples--;
    if (_expectedDataOutputSamples == 0) {
      _gatherer.functionInvoked(_configuration, _functionIdentifier, 1, _invocationTime, _dataInputBytes, (_dataOutputSamples > 0) ? _dataOutputBytes / _dataOutputSamples : Double.NaN);
    }
  }

}
