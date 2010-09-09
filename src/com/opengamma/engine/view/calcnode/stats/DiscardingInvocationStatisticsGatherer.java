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
  public void functionInvoked(final String functionIdentifier, final long invocationTime, final long dataInputVolume, final long dataOutputVolume) {
    // No action
  }

}
