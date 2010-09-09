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

  void functionInvoked(String configurationName, String functionIdentifier, int count, double invocationTime, double dataInput, double dataOutput);

}
