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
  public static final String LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD = "LocalVolatilitySurfaceCalculationMethod";
  public static final String DUPIRE_LOCAL_SURFACE_METHOD = "DupireLocalSurfaceMethod";

  // Properties for the y axis parameterisation type
  public static final String PROPERTY_Y_AXIS_PARAMETERIZATION = "LocalVolatilitySurfaceParameterization";
  public static final String MONEYNESS = "Moneyness";
  public static final String STRIKE = "Strike";

  // Properties for the Dupire method
  public static final String PROPERTY_DERIVATIVE_EPS = "SurfaceDerivativeEPS";
}
