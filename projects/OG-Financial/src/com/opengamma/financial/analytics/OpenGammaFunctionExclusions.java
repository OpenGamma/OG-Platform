/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

/**
 * Function exclusion groups for use with the OG-Analytics package.
 */
public final class OpenGammaFunctionExclusions {

  private OpenGammaFunctionExclusions() {
  }

  //CSOFF
  public static final String BLACK_VOLATILITY_SURFACE_DEFAULTS = "BLACK_VOLATILITY_SURFACE_DEFAULTS";
  public static final String BLACK_VOLATILITY_SURFACE_INTERPOLATOR_DEFAULTS = "BLACK_VOLATILITY_SURFACE_INTERPOLATOR_DEFAULTS";
  public static final String PDE_DEFAULTS = "PDE_DEFAULTS";
  public static final String LOCAL_VOLATILITY_SURFACE_DEFAULTS = "LOCAL_VOLATILITY_SURFACE_DEFAULTS";
  public static final String PNL_SERIES = "PNL_SERIES";
  public static final String INTEREST_RATE_INSTRUMENT_DEFAULTS = "INTEREST_RATE_INSTRUMENT_DEFAULTS";
  public static final String SABR_FITTING_DEFAULTS = "SABR_FITTING_DEFAULTS";
  public static final String FX_FORWARD_DEFAULTS = "FX_FORWARD_DEFAULTS";
  public static final String FX_OPTION_BLACK_DEFAULTS = "FX_OPTION_BLACK_DEFAULTS";
  public static final String NORMAL_HISTORICAL_VAR = "NORMAL_HISTORICAL_VAR";
  public static final String EMPIRICAL_HISTORICAL_VAR = "EMPIRICAL_HISTORICAL_VAR";
  public static final String SWAPTION_BLACK_DEFAULTS = "SWAPTION_BLACK_DEFAULTS";
  public static final String INTEREST_RATE_FUTURE = "INTEREST_RATE_FUTURE";
  public static final String IR_FUTURE_OPTION_BLACK = "IR_FUTURE_OPTION_BLACK";
  //CSON

}
