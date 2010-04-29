/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.HashMap;
import java.util.Map;

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
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    final Map<String, Interpolator1D> staticInstances = new HashMap<String, Interpolator1D>();
    final Map<Class<?>, String> instanceNames = new HashMap<Class<?>, String>();
    staticInstances.put(LINEAR, LINEAR_INSTANCE);
    instanceNames.put(LinearInterpolator1D.class, LINEAR);
    staticInstances.put(EXPONENTIAL, EXPONENTIAL_INSTANCE);
    instanceNames.put(ExponentialInterpolator1D.class, EXPONENTIAL);
    staticInstances.put(LOG_LINEAR, LOG_LINEAR_INSTANCE);
    instanceNames.put(LogLinearInterpolator1D.class, LOG_LINEAR);
    staticInstances.put(NATURAL_CUBIC_SPLINE, NATURAL_CUBIC_SPLINE_INSTANCE);
    instanceNames.put(NaturalCubicSplineInterpolator1D.class, NATURAL_CUBIC_SPLINE);
    s_staticInstances = new HashMap<String, Interpolator1D>(staticInstances);
    s_instanceNames = new HashMap<Class<?>, String>(instanceNames);
  }

  private Interpolator1DFactory() {
  }

  public static Interpolator1D getInterpolator(final String interpolatorName) {
    final Interpolator1D interpolator = s_staticInstances.get(interpolatorName);
    if (interpolator != null) {
      return interpolator;
    }
    // TODO kirk 2009-12-30 -- Deal with degree for Barycentric, Polynomial, and
    // RationalFunction
    throw new IllegalArgumentException("Interpolator not handled: " + interpolatorName);
  }

  public static String getInterpolatorName(final Interpolator1D interpolator) {
    if (interpolator == null) {
      return null;
    }
    final String interpolatorName = s_instanceNames.get(interpolator.getClass());
    // TODO kirk 2010-03-31 -- Deal with the more complicated rules for
    // Barycentric, Polynomial, and RationalFunction.
    return interpolatorName;
  }

}
