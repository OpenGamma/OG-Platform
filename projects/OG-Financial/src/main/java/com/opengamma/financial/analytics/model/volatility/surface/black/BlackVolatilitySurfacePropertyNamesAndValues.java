/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

/**
 *
 */
public class BlackVolatilitySurfacePropertyNamesAndValues {
  
  /** Property name for {@link ValuePropertyNames#SURFACE_CALCULATION_METHOD} 
   * indicating that an interpolated Black lognormal volatility surface was used
   */
  public static final String INTERPOLATED_BLACK_LOGNORMAL = "InterpolatedBlackLognormalSurface";

  // Properties and names for the time axis
  /** Property name for the time axis parameterization */
  public static final String PROPERTY_TIME_AXIS = "TimeAxis";
  /** Use log times on the time axis */
  public static final String LOG_TIME = "LogTime";
  /** Use linear times on the time axis */
  public static final String LINEAR_TIME = "LinearTime";

  // Properties and names for the y axis
  /** Property name for the y axis parameterization */
  public static final String PROPERTY_Y_AXIS = "YAxis";
  /** Use log y values */
  public static final String LOG_Y = "LogY";
  /** Use linear y values */
  public static final String LINEAR_Y = "LinearY";

  // Properties and names for the volatility transform
  /** Property name for the volatility transform */
  public static final String PROPERTY_VOLATILITY_TRANSFORM = "VolatilityTransform";
  /** Use integrated variance */
  public static final String INTEGRATED_VARIANCE = "IntegratedVariance";
  /** Use variance */
  public static final String VARIANCE = "Variance";

  // Properties for the time interpolator
  /** Property name for the time interpolator */
  public static final String PROPERTY_TIME_INTERPOLATOR = "TimeInterpolator";
  /** Property name for the left time extrapolator */
  public static final String PROPERTY_TIME_LEFT_EXTRAPOLATOR = "TimeLeftExtrapolator";
  /** Property name for the right time extrapolator */
  public static final String PROPERTY_TIME_RIGHT_EXTRAPOLATOR = "TimeRightExtrapolator";

  // Properties for the smile interpolator
  /** Property name for the smile interpolator */
  public static final String PROPERTY_SMILE_INTERPOLATOR = "SmileInterpolator";
  /** Use spline interpolation */
  public static final String SPLINE = "Spline";
  /** Use mixed log-normal interpolation */
  public static final String MIXED_LOG_NORMAL = "MixedLogNormal";
  /** Use SABR interpolation */
  public static final String SABR = "SABR";

  // Properties for spline smile interpolation
  /** Property name for the spline interpolator */
  public static final String PROPERTY_SPLINE_INTERPOLATOR = "SplineInterpolator";
  /** Property name for the left spline extrapolator */
  public static final String PROPERTY_SPLINE_LEFT_EXTRAPOLATOR = "SplineLeftExtrapolator";
  /** Property name for the right spline extrapolator */
  public static final String PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR = "SplineRightExtrapolator";

  // Properties and values for the spline smile extrapolation, which uses ShiftedLogNormalTailExtrapolationFitter
  /** Property name for the choice of whether to (a) throw an exception if the extrapolator fails
   *  to fit a ShiftedLognormal model to the vol and gradient of the last strike,
   *  (b) to reduce the slope until a fit is found or
   *  (c) to set slope to zero, such that exterior volatilities equal the last marked value 
   */
  public static final String PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE = "SplineExtrapolatorFailure";
  /** Selection *Quiet* will throw away a volatility if ShiftedLogNormalTailExtrapolationFitter fails and try the next nearest strike until it fits */
  public static final String QUIET_SPLINE_EXTRAPOLATOR_FAILURE = "Quiet";
  /**  Selection *Exception* puts the onus of shepherding the data on whoever provides marks */
  public static final String EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE = "Exception";
  /**  Selection *Flat* means that volatilities in the exterior will equal the vol at the nearest boundary*/
  public static final String FLAT_SPLINE_EXTRAPOLATOR_FAILURE = "Flat";
  
  // Properties for mixed log normal interpolation
  /** Property name for the weighting function to be used in mixed log-normal interpolation */
  public static final String PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION = "MixedLogNormalWeightingFunction";

  // Properties for SABR interpolation
  /** Use an externally-supplied beta value for SABR interpolation */
  public static final String PROPERTY_SABR_USE_EXTERNAL_BETA = "SABRUseExternalBeta";
  /** Property name for the externally-supplied value of beta */
  public static final String PROPERTY_SABR_EXTERNAL_BETA = "SABRBeta";
  /** Property name for the variation of the SABR model to be used */
  public static final String PROPERTY_SABR_MODEL = "SABRModel";
  /** Property name for the weighting function to be used in SABR interpolation */
  public static final String PROPERTY_SABR_WEIGHTING_FUNCTION = "SABRWeightingFunction";


}
