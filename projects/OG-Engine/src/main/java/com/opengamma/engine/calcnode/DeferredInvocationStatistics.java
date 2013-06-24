/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import com.codahale.metrics.Timer;
import com.opengamma.engine.cache.DeferredStatistics;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/**
 * Statistics for defered invocations. Some values exposed with {@code OpenGammaMetricRegistry}.
 */
/* package */ class DeferredInvocationStatistics implements DeferredStatistics {

  private final FunctionInvocationStatisticsGatherer _gatherer;
  private final String _configuration;
  private String _functionIdentifier;
  private double _dataInputBytes;
  private int _dataOutputBytes;
  private int _dataOutputSamples;
  private int _expectedDataOutputSamples;
  private Timer _timer;
  private Timer.Context _context;
  //TODO: Look at replacing (or simply exposing) IO metrics

  protected DeferredInvocationStatistics(final FunctionInvocationStatisticsGatherer gatherer, final String configuration, final String functionIdentifier) {
    _gatherer = gatherer;
    _configuration = configuration;
    _functionIdentifier = functionIdentifier;
    _timer = OpenGammaMetricRegistry.getDetailedInstance().timer(functionIdentifier + ".invoke");
  }

  protected void beginInvocation() {
    _context = _timer.time();
  }

  protected void endInvocation() {
    _context.close();
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
      _gatherer.functionInvoked(_configuration, _functionIdentifier, 1, _timer.getSnapshot().getMean(), _dataInputBytes, (_dataOutputSamples > 0) ? _dataOutputBytes / _dataOutputSamples : Double.NaN);
    }
  }

}
