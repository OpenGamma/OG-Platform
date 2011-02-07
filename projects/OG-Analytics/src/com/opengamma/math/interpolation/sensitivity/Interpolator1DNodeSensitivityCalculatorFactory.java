/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public final class Interpolator1DNodeSensitivityCalculatorFactory {
  /** Linear instance */
  public static final LinearInterpolator1DNodeSensitivityCalculator LINEAR_NODE_SENSITIVITY_CALCULATOR = new LinearInterpolator1DNodeSensitivityCalculator();
  /** Double quadratic instance */
  public static final DoubleQuadraticInterpolator1DNodeSensitivityCalculator DOUBLE_QUADRATIC_NODE_SENSITIVITY_CALCULATOR = new DoubleQuadraticInterpolator1DNodeSensitivityCalculator();
  /** Natural cubic spline instance */
  public static final NaturalCubicSplineInterpolator1DNodeSensitivityCalculator NATURAL_CUBIC_SPLINE_NODE_SENSITIVITY_CALCULATOR = new NaturalCubicSplineInterpolator1DNodeSensitivityCalculator();
  /** Flat extrapolator instance */
  public static final FlatExtrapolator1DNodeSensitivityCalculator FLAT_EXTRAPOLATOR_INSTANCE = new FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>();
  /** Finite difference */
  public static final String FINITE_DIFFERENCE = "Finite Difference";
  private static final Map<String, Interpolator1DNodeSensitivityCalculator> s_staticInstances = new HashMap<String, Interpolator1DNodeSensitivityCalculator>();

  static {
    s_staticInstances.put(Interpolator1DFactory.LINEAR, LINEAR_NODE_SENSITIVITY_CALCULATOR);
    s_staticInstances.put(Interpolator1DFactory.DOUBLE_QUADRATIC, DOUBLE_QUADRATIC_NODE_SENSITIVITY_CALCULATOR);
    s_staticInstances.put(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, NATURAL_CUBIC_SPLINE_NODE_SENSITIVITY_CALCULATOR);
    s_staticInstances.put(Interpolator1DFactory.FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR_INSTANCE);
  }

  private Interpolator1DNodeSensitivityCalculatorFactory() {
  }

  public static Interpolator1DNodeSensitivityCalculator getSensitivityCalculator(final String sensitivityCalculatorName, final boolean useFiniteDifferenceByDefault) {
    if (useFiniteDifferenceByDefault) {
      return new FiniteDifferenceInterpolator1DNodeSensitivityCalculator(Interpolator1DFactory.getInterpolator(sensitivityCalculatorName));
    }
    final Interpolator1DNodeSensitivityCalculator calculator = s_staticInstances.get(sensitivityCalculatorName);
    if (calculator != null) {
      return calculator;
    }
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(sensitivityCalculatorName);
    return new FiniteDifferenceInterpolator1DNodeSensitivityCalculator(interpolator);
  }
}
