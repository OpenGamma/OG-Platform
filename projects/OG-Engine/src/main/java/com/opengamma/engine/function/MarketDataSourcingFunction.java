/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;


/**
 * Special case of function implementation that is never executed by the graph executor but is used to source market data. It will not be considered directly during graph construction; the singleton
 * instance is associated with DependencyNode objects to act as a marker on the node.
 * <p>
 * This should never be present in a function repository as it should never be selected for execution.
 */
public final class MarketDataSourcingFunction extends IntrinsicFunction {

  /**
   * Singleton instance.
   */
  public static final MarketDataSourcingFunction INSTANCE = new MarketDataSourcingFunction();

  /**
   * Function unique ID.
   */
  public static final String UNIQUE_ID = "MarketDataSourcingFunction";

  private MarketDataSourcingFunction() {
    super(UNIQUE_ID);
  }

}
