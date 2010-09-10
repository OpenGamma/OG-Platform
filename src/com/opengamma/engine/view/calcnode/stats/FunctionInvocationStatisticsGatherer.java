/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

/**
 * Receives statistics on function execution from calculation node. The statistics must be normalized to a
 * common reference to eliminate differences in node performance.
 */
public interface FunctionInvocationStatisticsGatherer {

  /**
   * Records a successful function invocation.
   * 
   * The data input/output should be a volume measure, but could be the time to write to cache if that is more convenient to obtain.
   * The scaling behaviors of the {@link FunctionInvocationStatisticsSender} should compensate for any difference in measurement
   * techniques used by different nodes.
   * 
   * @param configurationName the configuration
   * @param functionIdentifier the function
   * @param count number of invocations the data is for
   * @param invocationTime execution time, in nanoseconds, of the invocation(s)
   * @param dataInput mean data input per input node, or {@code NaN} to mean statistics aren't available
   * @param dataOutput mean data output per output node, or {@code NaN} to mean statistics aren't available
   */
  void functionInvoked(String configurationName, String functionIdentifier, int count, double invocationTime, double dataInput, double dataOutput);

}
