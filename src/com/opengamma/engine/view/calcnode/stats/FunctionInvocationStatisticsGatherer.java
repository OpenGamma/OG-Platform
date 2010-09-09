/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

/**
 * Receives statistics on function execution from calculation node.
 */
public interface FunctionInvocationStatisticsGatherer {

  void functionInvoked(String functionIdentifier, long invocationTime, long dataInputVolume, long dataOutputVolume);

}
