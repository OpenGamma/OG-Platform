/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

/**
 * Records statistics for a given function. Time and data volumes are accumulated and a snapshot taken
 * every so-many samples to indicate cost. Old data is decayed to be less relevant.
 */
public class FunctionInvocationStatistics {

  private static final double DATA_DECAY = 0.1;

  private static final int SNAPSHOT_SAMPLES = 100;

  private final String _functionIdentifier;
  private double _invocations;
  private double _invocationTime;
  private double _dataInput;
  private double _dataOutput;

  private int _cost = SNAPSHOT_SAMPLES - 1;
  private double _invocationCost = 1.0;
  private double _dataInputCost = 1.0;
  private double _dataOutputCost = 1.0;

  protected FunctionInvocationStatistics(final String functionIdentifier) {
    _functionIdentifier = functionIdentifier;
  }

  protected synchronized void recordInvocation(final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    _invocations += count;
    _invocationTime += invocationTime;
    _dataInput += Double.isNaN(dataInput) ? ((_invocations > 0) ? (_dataInput / _invocations) : 0) : dataInput;
    _dataOutput += Double.isNaN(dataOutput) ? ((_invocations > 0) ? (_dataOutput / _invocations) : 0) : dataOutput;
    _cost += count;
    if (_cost >= SNAPSHOT_SAMPLES) {
      _cost = 0;
      _invocationCost = _invocationTime / _invocations;
      _dataInputCost = _dataInput / _invocations;
      _dataOutputCost = _dataOutput / _invocations;
      _invocations *= 1 - DATA_DECAY;
      _invocationTime *= 1 - DATA_DECAY;
      _dataInput *= 1 - DATA_DECAY;
      _dataOutput *= 1 - DATA_DECAY;
    }
  }

  public String getFunctionIdentifier() {
    return _functionIdentifier;
  }

  public double getInvocationCost() {
    return _invocationCost;
  }

  public double getDataInputCost() {
    return _dataInputCost;
  }

  public double getDataOutputCost() {
    return _dataOutputCost;
  }

}
