/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
@SuppressWarnings("unchecked")
public final class CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory {

  private CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory() {
  }

  public static CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> getSensitivityCalculator(final String interpolatorName,
      final boolean useFiniteDifferenceByDefault) {
    final Interpolator1DNodeSensitivityCalculator sensitivityCalculator = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(interpolatorName, useFiniteDifferenceByDefault);
    return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator);
  }

  public static CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> getSensitivityCalculator(final String interpolatorName, final String extrapolatorName,
      final boolean useFiniteDifferenceByDefault) {
    final Interpolator1DNodeSensitivityCalculator sensitivityCalculator = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(interpolatorName, useFiniteDifferenceByDefault);
    if (extrapolatorName == null || extrapolatorName.isEmpty()) {
      return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator);
    }
    final Interpolator1DNodeSensitivityCalculator extrapolatingSensitivityCalculator = getExtrapolatingSensitivityCalculator(extrapolatorName, sensitivityCalculator);
    return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator, extrapolatingSensitivityCalculator, extrapolatingSensitivityCalculator);
  }

  //REVIEW see review in CombinedInterpolatorExtrapolatorFactory - the behaviour should be the same in both cases
  public static CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> getSensitivityCalculator(final String interpolatorName,
      final String leftExtrapolatorName, final String rightExtrapolatorName, final boolean useFiniteDifferenceByDefault) {
    final Interpolator1DNodeSensitivityCalculator sensitivityCalculator = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(interpolatorName, useFiniteDifferenceByDefault);
    if (leftExtrapolatorName == null || leftExtrapolatorName.isEmpty()) {
      if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
        return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator);
      }
      final Interpolator1DNodeSensitivityCalculator extrapolator = getExtrapolatingSensitivityCalculator(rightExtrapolatorName, sensitivityCalculator);
      return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator, extrapolator);
    }
    if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
      final Interpolator1DNodeSensitivityCalculator extrapolator = getExtrapolatingSensitivityCalculator(leftExtrapolatorName, sensitivityCalculator);
      return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator, extrapolator);
    }
    final Interpolator1DNodeSensitivityCalculator leftExtrapolator = getExtrapolatingSensitivityCalculator(leftExtrapolatorName, sensitivityCalculator);
    final Interpolator1DNodeSensitivityCalculator rightExtrapolator = getExtrapolatingSensitivityCalculator(rightExtrapolatorName, sensitivityCalculator);
    return new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(sensitivityCalculator, leftExtrapolator, rightExtrapolator);

  }

  private static Interpolator1DNodeSensitivityCalculator getExtrapolatingSensitivityCalculator(final String extrapolatorName, final Interpolator1DNodeSensitivityCalculator sensitivityCalculator) {
    if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
      return Interpolator1DNodeSensitivityCalculatorFactory.FLAT_EXTRAPOLATOR_INSTANCE;
    } else if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
      return new LinearExtrapolator1DNodeSensitivityCalculator(sensitivityCalculator);
    }
    throw new IllegalArgumentException("Cannot get extrapolating sensitivity calculator " + extrapolatorName);
  }
}
