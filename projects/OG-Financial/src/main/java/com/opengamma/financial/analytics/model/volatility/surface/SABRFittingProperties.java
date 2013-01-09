/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

/**
 *
 */
public class SABRFittingProperties {
  /** Fix alpha during fitting */
  public static final String PROPERTY_USE_FIXED_ALPHA = "UseFixedAlpha";
  /** Fixed alpha value */
  public static final String PROPERTY_ALPHA = "Alpha";
  /** Fix beta during fitting */
  public static final String PROPERTY_USE_FIXED_BETA = "UseFixedBeta";
  /** Fixed beta value */
  public static final String PROPERTY_BETA = "Beta";
  /** Fix nu during fitting */
  public static final String PROPERTY_USE_FIXED_NU = "UseFixedNu";
  /** Fixed nu value */
  public static final String PROPERTY_NU = "Nu";
  /** Fix rho during fitting */
  public static final String PROPERTY_USE_FIXED_RHO = "UseFixedRho";
  /** Fixed rho value */
  public static final String PROPERTY_RHO = "Rho";
  /** The error in volatility quotes */
  public static final String PROPERTY_ERROR = "Error";
}
