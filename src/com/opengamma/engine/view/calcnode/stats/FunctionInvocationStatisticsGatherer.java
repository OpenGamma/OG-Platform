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
   * @param configurationName the configuration
   * @param functionIdentifier the function
   * @param count number of invocations the data is for
   * @param invocationTime execution time, in nanoseconds, of the invocation(s)
   * @param dataInput mean data input, bytes per input node, or {@code NaN} to mean statistics aren't available
   * @param dataOutput mean data output, bytes per output node, or {@code NaN} to mean statistics aren't available
   */
  void functionInvoked(String configurationName, String functionIdentifier, int count, double invocationTime, double dataInput, double dataOutput);

}
