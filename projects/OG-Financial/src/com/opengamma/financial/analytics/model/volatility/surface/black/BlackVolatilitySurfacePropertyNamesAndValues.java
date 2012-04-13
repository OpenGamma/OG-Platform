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
  // Properties and names for the time axis
  public static final String PROPERTY_TIME_AXIS = "TimeAxis";
  public static final String LOG_TIME = "LogTime";
  public static final String LINEAR_TIME = "LinearTime";

  // Properties and names for the y axis
  public static final String PROPERTY_Y_AXIS = "YAxis";
  public static final String LOG_Y = "LogY";
  public static final String LINEAR_Y = "LinearY";

  // Properties and names for the volatility transform
  public static final String PROPERTY_VOLATILITY_TRANSFORM = "VolatilityTransform";
  public static final String INTEGRATED_VARIANCE = "IntegratedVariance";
  public static final String VARIANCE = "Variance";

  // Properties for the time interpolator
  public static final String PROPERTY_TIME_INTERPOLATOR = "TimeInterpolator";
  public static final String PROPERTY_TIME_LEFT_EXTRAPOLATOR = "TimeLeftExtrapolator";
  public static final String PROPERTY_TIME_RIGHT_EXTRAPOLATOR = "TimeRightExtrapolator";

  // Properties for the smile interpolator
  public static final String PROPERTY_SMILE_INTERPOLATOR = "SmileInterpolator";
  public static final String SPLINE = "Spline";
  public static final String MIXED_LOG_NORMAL = "MixedLogNormal";
  public static final String SABR = "SABR";

  // Properties for spline smile interpolation
  public static final String PROPERTY_SPLINE_INTERPOLATOR = "SplineInterpolator";
  public static final String PROPERTY_SPLINE_LEFT_EXTRAPOLATOR = "SplineLeftExtrapolator";
  public static final String PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR = "SplineRightExtrapolator";

  // Properties for mixed log normal interpolation
  public static final String PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION = "MixedLogNormalWeightingFunction";

  // Properties for SABR interpolation
  public static final String PROPERTY_SABR_USE_EXTERNAL_BETA = "SABRUseExternalBeta";
  public static final String PROPERTY_SABR_EXTERNAL_BETA = "SABRBeta";
  public static final String PROPERTY_SABR_MODEL = "SABRModel";
  public static final String PROPERTY_SABR_WEIGHTING_FUNCTION = "SABRWeightingFunction";
}
