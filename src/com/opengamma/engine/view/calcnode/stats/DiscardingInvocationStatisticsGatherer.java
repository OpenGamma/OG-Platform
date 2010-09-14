/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

/**
 * Discards the statistics.
 */
public class DiscardingInvocationStatisticsGatherer implements FunctionInvocationStatisticsGatherer {

  @Override
  public void functionInvoked(final String configurationName, final String functionIdentifier, final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    // No action
  }

}
