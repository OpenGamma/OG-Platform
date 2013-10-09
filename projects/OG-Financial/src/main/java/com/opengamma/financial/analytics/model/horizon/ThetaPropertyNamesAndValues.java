/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

/**
 *
 */
public class ThetaPropertyNamesAndValues {
  /** Property describing the theta calculation method */
  public static final String PROPERTY_THETA_CALCULATION_METHOD = "ThetaCalculationMethod";
  /** Value indicating constant spread horizon calculations */
  public static final String THETA_CONSTANT_SPREAD = "ConstantSpread";
  /** Value indicating forward slide horizon calculations */
  public static final String THETA_FORWARD_SLIDE = "ForwardSlide";
  /** Property describing the number of days to move in horizon calculations */
  public static final String PROPERTY_DAYS_TO_MOVE_FORWARD = "DaysForward";
  /** Value indicating that only yield curves are to be rolled forward */
  public static final String THETA_CONSTANT_SPREAD_YIELD_CURVES = "ConstantSpreadYieldCurves";
  /** Value indicating that only volatility surfaces are to be rolled forward */
  public static final String THETA_CONSTANT_SPREAD_VOLATILITY_SURFACE = "ConstantSpreadVolatilitySurface";
  /** Yield curve forward slide value */
  public static final String THETA_FORWARD_SLIDE_YIELD_CURVES = "ForwardSlideYieldCurves";
  /** Volatility surface forward slide value */
  public static final String THETA_FORWARD_SLIDE_VOLATILITY_SURFACE = "ForwardSlideVolatilitySurface";
  /** Value indicating a theoretical theta value (i.e. the number calculated by differentiating the
   * pricing equation once with respect to time */
  public static final String OPTION_THETA = "OptionTheta";
  /** The default number of days in a year */
  public static final double DEFAULT_DAYS_PER_YEAR = 365.25;

}
