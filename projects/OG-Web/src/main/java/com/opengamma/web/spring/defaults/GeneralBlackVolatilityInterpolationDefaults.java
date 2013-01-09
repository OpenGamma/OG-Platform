/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 * Class containing default values for Black volatility surface interpolation. 
 * At the moment, these defaults are the same for all underlyings and security types, but
 * could be changed to be more flexible. 
 */
public class GeneralBlackVolatilityInterpolationDefaults {
  private static final List<String> SABR_INTERPOLATION_DEFAULTS;
  private static final List<String> MIXED_LOG_NORMAL_INTERPOLATION_DEFAULTS;
  private static final List<String> SPLINE_INTERPOLATION_DEFAULTS;
  
  static {
    List<String> commonProperties = Arrays.asList(
        BlackVolatilitySurfacePropertyNamesAndValues.LOG_TIME,
        BlackVolatilitySurfacePropertyNamesAndValues.LOG_Y,
        BlackVolatilitySurfacePropertyNamesAndValues.INTEGRATED_VARIANCE,
        Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    SABR_INTERPOLATION_DEFAULTS = new ArrayList<String>(commonProperties);
    SABR_INTERPOLATION_DEFAULTS.add(VolatilityFunctionFactory.HAGAN);
    SABR_INTERPOLATION_DEFAULTS.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    SABR_INTERPOLATION_DEFAULTS.add("false");
    SABR_INTERPOLATION_DEFAULTS.add("0.5");
    MIXED_LOG_NORMAL_INTERPOLATION_DEFAULTS = new ArrayList<String>(commonProperties);
    MIXED_LOG_NORMAL_INTERPOLATION_DEFAULTS.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    SPLINE_INTERPOLATION_DEFAULTS = new ArrayList<String>(commonProperties);
    SPLINE_INTERPOLATION_DEFAULTS.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    SPLINE_INTERPOLATION_DEFAULTS.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    SPLINE_INTERPOLATION_DEFAULTS.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    SPLINE_INTERPOLATION_DEFAULTS.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
  }
  
  /**
   * Gets the default values for SABR interpolation of Black surfaces
   * @return A list containing the default values
   */
  public static List<String> getSABRInterpolationDefaults() {
    return SABR_INTERPOLATION_DEFAULTS;
  }
  
  /**
   * Gets the default values for mixed log normal interpolation of Black surfaces
   * @return A list containing the default values
   */
  public static List<String> getMixedLogNormalInterpolationDefaults() {
    return MIXED_LOG_NORMAL_INTERPOLATION_DEFAULTS;
  }
  
  /**
   * Gets the default values for spline interpolation of Black surfaces
   * @return A list containing the default values
   */
  public static List<String> getSplineInterpolationDefaults() {
    return SPLINE_INTERPOLATION_DEFAULTS;
  }
    
}
