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
  private long _lastUpdated;

  protected FunctionInvocationStatistics(final String functionIdentifier) {
    _functionIdentifier = functionIdentifier;
  }

  protected synchronized void setCosts(final double invocationCost, final double dataInputCost, final double dataOutputCost) {
    _invocationCost = invocationCost;
    _dataInputCost = dataInputCost;
    _dataOutputCost = dataOutputCost;
    _lastUpdated = System.nanoTime();
  }

  protected synchronized void recordInvocation(final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    _invocations += count;
    _invocationTime += invocationTime;
    _dataInput += Double.isNaN(dataInput) ? ((_invocations > 0) ? (_dataInput / _invocations) : 0) : dataInput;
    _dataOutput += Double.isNaN(dataOutput) ? ((_invocations > 0) ? (_dataOutput / _invocations) : 0) : dataOutput;
    _cost += count;
    if (_cost >= SNAPSHOT_SAMPLES) {
      _cost = 0;
      setCosts(_invocationTime / _invocations, _dataInput / _invocations, _dataOutput / _invocations);
      _invocations *= 1 - DATA_DECAY;
      _invocationTime *= 1 - DATA_DECAY;
      _dataInput *= 1 - DATA_DECAY;
      _dataOutput *= 1 - DATA_DECAY;
    }
  }

  public String getFunctionIdentifier() {
    return _functionIdentifier;
  }

  /**
   * Invocation cost - mean "standard" time to execute in nanoseconds.
   * 
   * @return invocation cost in nanoseconds
   */
  public double getInvocationCost() {
    return _invocationCost;
  }

  /**
   * Data input cost - mean bytes per input value.
   * 
   * @return data input cost in bytes per value
   */
  public double getDataInputCost() {
    return _dataInputCost;
  }

  /**
   * Data output cost - mean bytes per output value.
   * 
   * @return data output cost in bytes per value
   */
  public double getDataOutputCost() {
    return _dataOutputCost;
  }

  /**
   * Returns the {@link System#nanoTime} timestamp of the last time the costs changed.
   * 
   * @return the value of {@link System#nanoTime} of the last sample update
   */
  public long getLastUpdateNanos() {
    return _lastUpdated;
  }

  @Override
  public String toString() {
    return getFunctionIdentifier() + " = " + getInvocationCost() + "ns, " + getDataInputCost() + " bytes/input, " + getDataOutputCost() + " bytes/output, at " + getLastUpdateNanos();
  }

}
