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
  public static final String DELTA_BLEED = "DeltaBleed";
  public static final String STRIKE_DELTA = "StrikeDelta";
  public static final String DRIFTLESS_DELTA = "DriftlessTheta";
  
  public static final String GAMMA = "Gamma";
  public static final String GAMMA_P = "GammaP";
  public static final String STRIKE_GAMMA = "StrikeGamma";
  public static final String GAMMA_BLEED = "GammaBleed";
  public static final String GAMMA_P_BLEED = "GammaPBleed";
  
  public static final String VEGA = "Vega";
  public static final String VEGA_P = "VegaP";
  public static final String VARIANCE_VEGA = "VarianceVega";
  public static final String VEGA_BLEED = "VegaBleed";
  
  public static final String THETA = "Theta";
  
  public static final String RHO = "Rho";
  public static final String TIME_BUCKETED_RHO = "TimeBucketedRho";
  public static final String CARRY_RHO = "CarryRho";
  
  public static final String ZETA = "Zeta";
  public static final String ZETA_BLEED = "ZetaBleed";
  public static final String DZETA_DVOL = "dZeta_dVol";
  
  public static final String ELASTICITY = "Elasticity";
  public static final String PHI = "Phi";
  
  public static final String ZOMMA = "Zomma";
  public static final String ZOMMA_P = "ZommaP";
  
  public static final String ULTIMA = "Ultima";
  public static final String VARIANCE_ULTIMA = "VarianceUltima";
  
  public static final String SPEED = "Speed";
  public static final String SPEED_P = "SpeedP";
  
  public static final String VANNA = "Vanna";
  public static final String VARIANCE_VANNA = "VarianceVanna";
  public static final String DVANNA_DVOL = "dVanna_dVol";
  
  public static final String VOMMA = "Vomma";
  public static final String VOMMA_P = "VommaP";
  public static final String VARIANCE_VOMMA = "VarianceVomma";

  // Modified Greeks:
  public static final String DOLLAR_DELTA = "DollarDelta";
  
  // Generic Aggregates:
  public static final String SUM = "Sum";
  public static final String MEDIAN = "Median";
  
  // Risk Aggregates:
  public static final String ISOLATED_VAR = "IsolatedVaR";
  public static final String INCREMENTAL_VAR = "IncrementalVaR";

}
