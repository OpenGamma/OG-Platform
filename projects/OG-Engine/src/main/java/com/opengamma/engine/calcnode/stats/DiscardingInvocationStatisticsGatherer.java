/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

/**
 * Gatherer implementation that discards all received statistics.
 */
public class DiscardingInvocationStatisticsGatherer implements FunctionInvocationStatisticsGatherer {

  @Override
  public void functionInvoked(
      String configurationName, String functionId, int invocationCount,
      double executionNanos, double dataInputBytes, double dataOutputBytes) {
    // no action
  }

}
