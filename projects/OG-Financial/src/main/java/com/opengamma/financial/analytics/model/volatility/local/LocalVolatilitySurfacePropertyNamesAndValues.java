/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

/**
 *
 */
public class LocalVolatilitySurfacePropertyNamesAndValues {
  // Properties for the Dupire local volatility surface calculation method
  /** Property name of the local volatility surface calculation method */
  public static final String PROPERTY_LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD = "LocalVolatilitySurfaceCalculationMethod";
  /** The Dupire local volatility calculation method */
  public static final String DUPIRE_LOCAL_SURFACE_METHOD = "DupireLocalSurfaceMethod";

  // Properties for the y axis parameterisation type
  /** Property name of the y-axis parameterization */
  public static final String PROPERTY_Y_AXIS_PARAMETERIZATION = "LocalVolatilitySurfaceParameterization";
  /** Moneyness y-axis parameterization */
  public static final String MONEYNESS = "Moneyness";
  /** Strike y-axis parameterization */
  public static final String STRIKE = "Strike";

  // Properties for the Dupire method
  /** Property name for the eps to use when bumping the surface to calculate derivatives */
  public static final String PROPERTY_DERIVATIVE_EPS = "SurfaceDerivativeEPS";
}
