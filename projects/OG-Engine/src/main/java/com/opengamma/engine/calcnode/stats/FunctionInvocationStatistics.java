/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * Records statistics about invocations of a function.
 * <p>
 * This is run centrally to aggregate statistics. The statistics recorded include the time taken and the data volume. Old data is decayed to be less relevant.
 * <p>
 * This class is mutable and thread-safe via synchronization.
 */
public class FunctionInvocationStatistics {

  /**
   * A decay to prioritize the latest data.
   */
  private static final double DATA_DECAY = 0.1;
  /**
   * The number of samples in the snapshot.
   */
  private static final int SNAPSHOT_SAMPLES = 100;

  /**
   * The function identifier.
   */
  private final String _functionId;
  /**
   * A cost estimate for the time the function takes.
   */
  private double _invocationCost = 1.0;
  /**
   * A cost estimate for the data input size.
   */
  private double _dataInputCost = 1.0;
  /**
   * A cost estimate for the data output size.
   */
  private double _dataOutputCost = 1.0;
  /**
   * The {@link System#nanoTime} instant when the data was last updated.
   */
  private long _lastUpdated;

  private int _cost = SNAPSHOT_SAMPLES - 1;
  private double _invocations;
  private double _invocationTime;
  private double _dataInput;
  private double _dataOutput;

  /**
   * Creates an instance for a specific function.
   * 
   * @param functionId the function id, not null
   */
  public FunctionInvocationStatistics(final String functionId) {
    ArgumentChecker.notNull(functionId, "functionId");
    _functionId = functionId;
  }

  /**
   * Creates an instance from a document.
   * 
   * @param doc the document, not null
   */
  public FunctionInvocationStatistics(final FunctionCostsDocument doc) {
    ArgumentChecker.notNull(doc, "doc");
    _functionId = doc.getFunctionId();
    setCosts(doc.getInvocationCost(), doc.getDataInputCost(), doc.getDataOutputCost());
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the statistics record with details of the costs.
   * <p>
   * This may be called directly, but is more typically called via {@link #recordInvocation}.
   * 
   * @param invocationCost the invocation cost
   * @param dataInputCost the data input cost
   * @param dataOutputCost the data output cost
   */
  synchronized void setCosts(final double invocationCost, final double dataInputCost, final double dataOutputCost) {
    _invocationCost = invocationCost;
    _dataInputCost = dataInputCost;
    _dataOutputCost = dataOutputCost;
    _lastUpdated = System.nanoTime();
  }

  /**
   * Updates the statistics with details of one or more invocations.
   * <p>
   * The data passed in is used to update the long-running cost values.
   * 
   * @param invocationCount the number of invocations the data is for
   * @param invocationNanos the execution time, in nanoseconds, of the invocation(s)
   * @param dataInputBytes the mean data input, bytes per input node, or {@code NaN} if unavailable
   * @param dataOutputBytes the mean data output, bytes per output node, or {@code NaN} if unavailable
   */
  synchronized void recordInvocation(
      final int invocationCount, final double invocationNanos, final double dataInputBytes, final double dataOutputBytes) {
    _invocations += invocationCount;
    _invocationTime += invocationNanos;
    _dataInput += Double.isNaN(dataInputBytes) ? ((_invocations > 0) ? (_dataInput / _invocations) : 0) : dataInputBytes;
    _dataOutput += Double.isNaN(dataOutputBytes) ? ((_invocations > 0) ? (_dataOutput / _invocations) : 0) : dataOutputBytes;
    _cost += invocationCount;
    if (_cost >= SNAPSHOT_SAMPLES) {
      _cost = 0;
      setCosts(_invocationTime / _invocations, _dataInput / _invocations, _dataOutput / _invocations);
      _invocations *= 1 - DATA_DECAY;
      _invocationTime *= 1 - DATA_DECAY;
      _dataInput *= 1 - DATA_DECAY;
      _dataOutput *= 1 - DATA_DECAY;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the function identifier.
   * 
   * @return the function identifier, not null
   */
  public String getFunctionId() {
    return _functionId;
  }

  /**
   * Gets the invocation cost, a mean "standard" time to execute in nanoseconds.
   * 
   * @return invocation cost in nanoseconds
   */
  public synchronized double getInvocationCost() {
    return _invocationCost;
  }

  /**
   * Gets the data input cost, a mean bytes per input value.
   * 
   * @return data input cost in bytes per value
   */
  public synchronized double getDataInputCost() {
    return _dataInputCost;
  }

  /**
   * Gets the data output cost, a mean bytes per output value.
   * 
   * @return data output cost in bytes per value
   */
  public synchronized double getDataOutputCost() {
    return _dataOutputCost;
  }

  /**
   * Gets the {@link System#nanoTime} timestamp of the last time the costs changed.
   * 
   * @return the value of {@link System#nanoTime} of the last sample update
   */
  public synchronized long getLastUpdateNanos() {
    return _lastUpdated;
  }

  //-------------------------------------------------------------------------
  /**
   * Populates the document with a snapshot of the values from this class.
   * 
   * @param document the document to populate, not null
   */
  public synchronized void populateDocument(final FunctionCostsDocument document) {
    document.setInvocationCost(_invocationCost);
    document.setDataInputCost(_dataInputCost);
    document.setDataOutputCost(_dataOutputCost);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string suitable for debugging.
   * 
   * @return a string, not null
   */
  @Override
  public String toString() {
    return getFunctionId() + " = " + getInvocationCost() + "ns, " + getDataInputCost() + " bytes/input, " +
        getDataOutputCost() + " bytes/output, at " + getLastUpdateNanos();
  }

  // For debug purposes only, remove when PLAT-882 is complete
  /* package */synchronized FudgeMsg toFudgeMsg(final FudgeMsgFactory factory) { ///CSIGNORE
    final MutableFudgeMsg message = factory.newMessage();
    message.add("invocationCost", _invocationCost);
    message.add("dataInputCost", _dataInputCost);
    message.add("dataOutputCost", _dataOutputCost);
    message.add("invocationCount", _invocations);
    message.add("invocationTime", _invocationTime);
    message.add("dataInputSize", _dataInput);
    message.add("dataOutputSize", _dataOutput);
    return message;
  }

}
