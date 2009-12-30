/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

/**
 * A collection of common/standard names for the Value Name property
 * for {@link ValueRequirement} instances.
 *
 * @author kirk
 */
public interface ValueRequirementNames {
  // Market Data Names:
  public static final String MARKET_DATA_HEADER = "MarketDataHeader";
  
  // Standard Analytic Models:
  public static final String DISCOUNT_CURVE = "DiscountCurve";
  public static final String VOLATILITY_SURFACE = "VolatilitySurface";
  
  //
  public static final String FAIR_VALUE = "FairValue";
  
  // Greeks Names:
  public static final String DELTA = "Delta";
  public static final String VEGA = "Vega";
  public static final String THETA = "Theta";
  public static final String RHO = "Rho";
  public static final String GAMMA = "Gamma";
  
  // Modified Greeks:
  public static final String DOLLAR_DELTA = "DollarDelta";
  
  // Generic Aggregates:
  public static final String SUM = "Sum";
  public static final String MEDIAN = "Median";
  
  // Risk Aggregates:
  public static final String ISOLATED_VAR = "IsolatedVaR";
  public static final String INCREMENTAL_VAR = "IncrementalVaR";

}
