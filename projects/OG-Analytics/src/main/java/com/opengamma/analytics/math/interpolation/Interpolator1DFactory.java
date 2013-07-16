/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.credit.cds.ISDAExtrapolator1D;
import com.opengamma.analytics.financial.credit.cds.ISDAInterpolator1D;

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
  /** Step with the value in the interval equal to the value at the upper bound */
  public static final String STEP_UPPER = "StepUpper";
  /** Double quadratic */
  public static final String DOUBLE_QUADRATIC = "DoubleQuadratic";
  /**Monotonicity-Preserving-Cubic-Spline
   * @deprecated Use the name PCHIP instead 
   * */
  @Deprecated
  public static final String MONOTONIC_CUBIC = "MonotonicityPreservingCubicSpline";
  /**Piecewise Cubic Hermite Interpolating Polynomial (PCHIP)*/
  public static final String PCHIP = "PCHIP";
  /**Modified Piecewise Cubic Hermite Interpolating Polynomial (PCHIP) for yield curves*/
  public static final String MOD_PCHIP = "ModifiedPCHIP";
  /** Time square */
  public static final String TIME_SQUARE = "TimeSquare";
  /** Flat extrapolator */
  public static final String FLAT_EXTRAPOLATOR = "FlatExtrapolator";
  /** Linear extrapolator */
  public static final String LINEAR_EXTRAPOLATOR = "LinearExtrapolator";
  /** Linear extrapolator */
  public static final String EXPONENTIAL_EXTRAPOLATOR = "ExponentialExtrapolator";
  /** ISDA interpolator */
  public static final String ISDA_INTERPOLATOR = "ISDAInterpolator";
  /** ISDA extrapolator */
  public static final String ISDA_EXTRAPOLATOR = "ISDAExtrapolator";
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
  /** Step-Upper instance */
  public static final StepUpperInterpolator1D STEP_UPPER_INSTANCE = new StepUpperInterpolator1D();
  /** Double quadratic instance */
  public static final DoubleQuadraticInterpolator1D DOUBLE_QUADRATIC_INSTANCE = new DoubleQuadraticInterpolator1D();
  /** MonotonicityPreservingCubicSpline
   * @deprecated use PCHIP_INSTANCE instead 
   * */
  @Deprecated
  public static final PCHIPInterpolator1D MONOTONIC_CUBIC_INSTANCE = new PCHIPInterpolator1D();
  /**Piecewise Cubic Hermite Interpolating Polynomial (PCHIP)*/
  public static final PCHIPInterpolator1D PCHIP_INSTANCE = new PCHIPInterpolator1D();
  /**Modified Piecewise Cubic Hermite Interpolating Polynomial (PCHIP) for yield curves*/
  public static final PCHIPYieldCurveInterpolator1D MOD_PCHIP_INSTANCE = new PCHIPYieldCurveInterpolator1D();
  /** Time square instance */
  public static final TimeSquareInterpolator1D TIME_SQUARE_INSTANCE = new TimeSquareInterpolator1D();
  /** Flat extrapolator instance */
  public static final FlatExtrapolator1D FLAT_EXTRAPOLATOR_INSTANCE = new FlatExtrapolator1D();
  /** Exponential extrapolator instance */
  public static final ExponentialExtrapolator1D EXPONENTIAL_EXTRAPOLATOR_INSTANCE = new ExponentialExtrapolator1D();
  /** ISDA interpolator instance */
  public static final ISDAInterpolator1D ISDA_INTERPOLATOR_INSTANCE = new ISDAInterpolator1D();
  /** ISDA extrapolator instance */
  public static final ISDAExtrapolator1D ISDA_EXTRAPOLATOR_INSTANCE = new ISDAExtrapolator1D();
  /** 
   */

  private static final Map<String, Interpolator1D> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    final Map<String, Interpolator1D> staticInstances = new HashMap<>();
    final Map<Class<?>, String> instanceNames = new HashMap<>();
    staticInstances.put(LINEAR, LINEAR_INSTANCE);
    instanceNames.put(LinearInterpolator1D.class, LINEAR);
    staticInstances.put(EXPONENTIAL, EXPONENTIAL_INSTANCE);
    instanceNames.put(ExponentialInterpolator1D.class, EXPONENTIAL);
    staticInstances.put(LOG_LINEAR, LOG_LINEAR_INSTANCE);
    instanceNames.put(LogLinearInterpolator1D.class, LOG_LINEAR);
    staticInstances.put(NATURAL_CUBIC_SPLINE, NATURAL_CUBIC_SPLINE_INSTANCE);
    instanceNames.put(NaturalCubicSplineInterpolator1D.class, NATURAL_CUBIC_SPLINE);
    staticInstances.put(STEP, STEP_INSTANCE);
    instanceNames.put(StepInterpolator1D.class, STEP);
    staticInstances.put(STEP_UPPER, STEP_UPPER_INSTANCE);
    instanceNames.put(StepUpperInterpolator1D.class, STEP_UPPER);
    staticInstances.put(DOUBLE_QUADRATIC, DOUBLE_QUADRATIC_INSTANCE);
    instanceNames.put(DoubleQuadraticInterpolator1D.class, DOUBLE_QUADRATIC);
    staticInstances.put(MONOTONIC_CUBIC, MONOTONIC_CUBIC_INSTANCE);
    instanceNames.put(PCHIPInterpolator1D.class, MONOTONIC_CUBIC);
    staticInstances.put(PCHIP, PCHIP_INSTANCE);
    instanceNames.put(PCHIPInterpolator1D.class, PCHIP);
    staticInstances.put(MOD_PCHIP, MOD_PCHIP_INSTANCE);
    instanceNames.put(PCHIPYieldCurveInterpolator1D.class, MOD_PCHIP);
    staticInstances.put(TIME_SQUARE, TIME_SQUARE_INSTANCE);
    instanceNames.put(TimeSquareInterpolator1D.class, TIME_SQUARE);
    staticInstances.put(FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR_INSTANCE);
    instanceNames.put(FlatExtrapolator1D.class, FLAT_EXTRAPOLATOR);
    staticInstances.put(EXPONENTIAL_EXTRAPOLATOR, EXPONENTIAL_EXTRAPOLATOR_INSTANCE);
    instanceNames.put(ExponentialExtrapolator1D.class, EXPONENTIAL_EXTRAPOLATOR);
    staticInstances.put(ISDA_INTERPOLATOR, ISDA_INTERPOLATOR_INSTANCE);
    instanceNames.put(ISDAInterpolator1D.class, ISDA_INTERPOLATOR);
    staticInstances.put(ISDA_EXTRAPOLATOR, ISDA_EXTRAPOLATOR_INSTANCE);
    instanceNames.put(ISDAExtrapolator1D.class, ISDA_EXTRAPOLATOR);

    s_staticInstances = new HashMap<>(staticInstances);
    s_instanceNames = new HashMap<>(instanceNames);
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
    return interpolatorName;
  }

}
