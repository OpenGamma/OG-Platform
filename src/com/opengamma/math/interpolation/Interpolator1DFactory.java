/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kirk
 */
public final class Interpolator1DFactory {
  public static final String LINEAR = "Linear";
  public static final LinearInterpolator1D LINEAR_INSTANCE = new LinearInterpolator1D();
  public static final String EXPONENTIAL = "Exponential";
  public static final ExponentialInterpolator1D EXPONENTIAL_INSTANCE = new ExponentialInterpolator1D();
  public static final String LOG_LINEAR = "LogLinear";
  public static final LogLinearInterpolator1D LOG_LINEAR_INSTANCE = new LogLinearInterpolator1D();
  public static final String NATURAL_CUBIC_SPLINE = "NaturalCubicSpline";
  public static final NaturalCubicSplineInterpolator1D NATURAL_CUBIC_SPLINE_INSTANCE = new NaturalCubicSplineInterpolator1D();
  
  public static final String BARYCENTRIC_RATIONAL_FUNCTION = "BarycentricRationalFunction";
  public static final String POLYNOMIAL = "Polynomial";
  public static final String RATIONAL_FUNCTION = "RationalFunction";
  
  private static final Map<String, Interpolator1D> s_staticInstances;
  
  static {
    Map<String, Interpolator1D> staticInstances = new HashMap<String, Interpolator1D>();
    staticInstances.put(LINEAR, LINEAR_INSTANCE);
    staticInstances.put(EXPONENTIAL, EXPONENTIAL_INSTANCE);
    staticInstances.put(LOG_LINEAR, LOG_LINEAR_INSTANCE);
    staticInstances.put(NATURAL_CUBIC_SPLINE, NATURAL_CUBIC_SPLINE_INSTANCE);
    s_staticInstances = new HashMap<String, Interpolator1D>(staticInstances);
  }
  
  private Interpolator1DFactory() {
  }
  
  public static Interpolator1D getInterpolator(String interpolatorName) {
    Interpolator1D interpolator = s_staticInstances.get(interpolatorName);
    if(interpolator != null) {
      return interpolator;
    }
    // TODO kirk 2009-12-30 -- Deal with degree for Barycentric, Polynomial, and RationalFunction
    throw new IllegalArgumentException("Interpolator not handled: " + interpolatorName);
  }

}
