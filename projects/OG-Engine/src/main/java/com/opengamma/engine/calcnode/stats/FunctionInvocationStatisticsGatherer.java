/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

/**
 * Receives statistics on function invocation within a calculation node.
 * <p>
 * The statistics must be normalized to a common reference to eliminate differences in node performance.
 */
public interface FunctionInvocationStatisticsGatherer {

  /**
   * Records a successful function invocation.
   * 
   * @param configurationName  the configuration name, not null
   * @param functionId  the function id, not null
   * @param invocationCount  the number of invocations the data is for
   * @param executionNanos  the execution time, in nanoseconds, of the invocation(s)
   * @param dataInputBytes  the mean data input, bytes per input node, or {@code NaN} to mean statistics aren't available
   * @param dataOutputBytes  the mean data output, bytes per output node, or {@code NaN} to mean statistics aren't available
   */
  void functionInvoked(
      String configurationName, String functionId, int invocationCount,
      double executionNanos, double dataInputBytes, double dataOutputBytes);

}
