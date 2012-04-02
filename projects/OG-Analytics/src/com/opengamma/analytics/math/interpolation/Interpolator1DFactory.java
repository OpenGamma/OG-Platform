/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class Interpolator1DFactory {
  /** Linear */
  public static final String LINEAR = "Linear";
  /** Exponential */
  public static final String EXPONENTIAL = "Exponential";
  /** Log-linear */
  public static final String LOG_LINEAR = "LogLinear";
  /** Natural cubic spline */
  public static final String NATURAL_CUBIC_SPLINE = "NaturalCubicSpline";
  /** Barycentric rational function */
  public static final String BARYCENTRIC_RATIONAL_FUNCTION = "BarycentricRationalFunction";
  /** Polynomial */
  public static final String POLYNOMIAL = "Polynomial";
  /** Rational function */
  public static final String RATIONAL_FUNCTION = "RationalFunction";
  /** Step */
  public static final String STEP = "Step";
  /** Double quadratic */
  public static final String DOUBLE_QUADRATIC = "DoubleQuadratic";
  /** Flat extrapolator */
  public static final String FLAT_EXTRAPOLATOR = "FlatExtrapolator";
  /** Linear extrapolator */
  public static final String LINEAR_EXTRAPOLATOR = "LinearExtrapolator";
  /** Linear instance */
  public static final LinearInterpolator1D LINEAR_INSTANCE = new LinearInterpolator1D();
  /** Exponential instance */
  public static final ExponentialInterpolator1D EXPONENTIAL_INSTANCE = new ExponentialInterpolator1D();
  /** Log-linear instance */
  public static final LogLinearInterpolator1D LOG_LINEAR_INSTANCE = new LogLinearInterpolator1D();
  /** Natural cubic spline instance */
  public static final NaturalCubicSplineInterpolator1D NATURAL_CUBIC_SPLINE_INSTANCE = new NaturalCubicSplineInterpolator1D();
  /** Step instance */
  public static final StepInterpolator1D STEP_INSTANCE = new StepInterpolator1D();
  /** Double quadratic instance */
  public static final DoubleQuadraticInterpolator1D DOUBLE_QUADRATIC_INSTANCE = new DoubleQuadraticInterpolator1D();

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
    staticInstances.put(DOUBLE_QUADRATIC, DOUBLE_QUADRATIC_INSTANCE);
    instanceNames.put(DoubleQuadraticInterpolator1D.class, DOUBLE_QUADRATIC);
    staticInstances.put(STEP, STEP_INSTANCE);
    instanceNames.put(StepInterpolator1D.class, STEP);
    instanceNames.put(FlatExtrapolator1D.class, FLAT_EXTRAPOLATOR);
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
    if (interpolator instanceof LinearExtrapolator1D) {
      return LINEAR_EXTRAPOLATOR;
    }
    if (interpolator instanceof FlatExtrapolator1D) {
      return FLAT_EXTRAPOLATOR;
    }
    return interpolatorName;
  }

}
