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
  public static final String THETA_CONSTANT_SPREAD = "HorizonConstantSpread";
  /** Property describing the number of days to move in horizon calculations */
  public static final String PROPERTY_DAYS_TO_MOVE_FORWARD = "DaysForward";
  /** Value indicating that only yield curves are to be rolled forward */
  public static final String THETA_CONSTANT_SPREAD_FORWARD_CURVE = "ConstantSpreadForwardCurve";
  /** Value indicating that only volatility surfaces are to be rolled forward */
  public static final String THETA_CONSTANT_SPREAD_VOLATILITY_SURFACE = "ConstantSpreadVolatilitySurface";

}
