/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public class LocalVolatilityPDEValuePropertyNames {
  /** Name of the PDE method */
  public static final String LOCAL_VOLATILITY_METHOD = "LocalVolatilityPDEMethod";
  /** Property name for the surface parameterization */
  public static final String PROPERTY_SURFACE_TYPE = "SurfaceParameterization";
  /** Name of the strike surface parameterization */
  public static final String STRIKE = "Strike";
  /** Name of the moneyness surface parameterization */
  public static final String MONEYNESS = "Moneyness";
  /** Property name for the time axis type */
  public static final String PROPERTY_X_AXIS = "TimeAxis";
  /** Name of the log time axis type */
  public static final String LOG_TIME = "LogTime";
  /** Name of the linear time axis */
  public static final String LINEAR_TIME = "LinearTime";
  /** Property name for the strike axis */
  public static final String PROPERTY_Y_AXIS = "YAxis";
  /** Name of the integrated variance y axis */
  public static final String INTEGRATED_VARIANCE = "IntegratedVariance";
  /** Name of the volatility y axis */
  public static final String VOLATILITY = "Volatility";
  /** Property name for the y axis type */
  public static final String PROPERTY_Y_AXIS_TYPE = "YAxisType";
  /** Property name for log y axis */
  public static final String LOG_Y = "LogY";
  /** Property name for linear strike axis */
  public static final String LINEAR_Y = "LinearY";
  /** Property name for the eps for local volatility surface bumping */
  public static final String PROPERTY_H = "h";
  /** Property name for the PDE direction */
  public static final String PROPERTY_PDE_DIRECTION = "PDEDirection";
  /** Name of the forward PDE type */
  public static final String FORWARD_PDE = "Forward PDE";
  /** Property name for the theta direction */
  public static final String PROPERTY_THETA = "Theta";
  /** Property name for the number of time steps */
  public static final String PROPERTY_TIME_STEPS = "TimeSteps";
  /** Property name for the number of space steps */
  public static final String PROPERTY_SPACE_STEPS = "SpaceSteps";
  /** Property name for the time grid bunching parameter */
  public static final String PROPERTY_TIME_GRID_BUNCHING = "TimeGridBunching";
  /** Property name for the space grid bunching parameter */
  public static final String PROPERTY_SPACE_GRID_BUNCHING = "SpaceGridBunching";
  /** Property name for the maximum moneyness to calculate the PDE */
  public static final String PROPERTY_MAX_MONEYNESS = "MaxMoneyness";
  /** Property name for the strike interpolator */
  public static final String PROPERTY_RESULT_STRIKE_INTERPOLATOR = "StrikeInterpolator";
  /** Property name for the time interpolator */
  public static final String PROPERTY_RESULT_TIME_INTERPOLATOR = "TimeInterpolator";
}
