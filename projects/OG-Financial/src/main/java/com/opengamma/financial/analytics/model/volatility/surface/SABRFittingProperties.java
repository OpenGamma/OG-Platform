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
  /** Surface fitting property */
  public static final String SABR = "SABR";
  /** Fix alpha during fitting */
  public static final String PROPERTY_USE_FIXED_ALPHA = "UseFixedAlpha";
  /** Fixed alpha value */
  public static final String PROPERTY_FIXED_ALPHA = "FixedAlpha";
  /** Starting value for alpha */
  public static final String PROPERTY_START_ALPHA = "StartAlpha";
  /** Fix beta during fitting */
  public static final String PROPERTY_USE_FIXED_BETA = "UseFixedBeta";
  /** Fixed beta value */
  public static final String PROPERTY_FIXED_BETA = "FixedBeta";
  /** Starting value for beta */
  public static final String PROPERTY_START_BETA = "StartBeta";
  /** Fix nu during fitting */
  public static final String PROPERTY_USE_FIXED_NU = "UseFixedNu";
  /** Fixed nu value */
  public static final String PROPERTY_FIXED_NU = "FixedNu";
  /** Starting value for nu */
  public static final String PROPERTY_START_NU = "StartNu";
  /** Fix rho during fitting */
  public static final String PROPERTY_USE_FIXED_RHO = "UseFixedRho";
  /** Fixed rho value */
  public static final String PROPERTY_FIXED_RHO = "FixedRho";
  /** Starting value for rho */
  public static final String PROPERTY_START_RHO = "StartRho";
  /** The error in volatility quotes */
  public static final String PROPERTY_ERROR = "Error";
}
