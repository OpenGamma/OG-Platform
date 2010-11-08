/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public final class CombinedInterpolatorExtrapolatorFactory {

  private CombinedInterpolatorExtrapolatorFactory() {
  }

  public static <T extends Interpolator1DDataBundle> CombinedInterpolatorExtrapolator<T> getInterpolator(final String interpolatorName) {
    final Interpolator1D<T> interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    return new CombinedInterpolatorExtrapolator<T>(interpolator);
  }

  public static <T extends Interpolator1DDataBundle> CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> getInterpolator(final String interpolatorName,
      final String extrapolatorName) {
    final Interpolator1D<T> interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (extrapolatorName == null || extrapolatorName.isEmpty()) {
      return new CombinedInterpolatorExtrapolator<T>(interpolator);
    }
    final Interpolator1D<T> extrapolator = getExtrapolator(extrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator<T>(interpolator, extrapolator, extrapolator);
  }

  // REVIEW emcleod 4-8-2010 not sure if this is how people will want to construct the combined interpolator - should it be more strict?
  // Also see CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
  public static <T extends Interpolator1DDataBundle> CombinedInterpolatorExtrapolator<T> getInterpolator(final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName) {
    final Interpolator1D<T> interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (leftExtrapolatorName == null || leftExtrapolatorName.isEmpty()) {
      if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
        return new CombinedInterpolatorExtrapolator<T>(interpolator);
      }
      final Interpolator1D<T> extrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator<T>(interpolator, extrapolator);
    }
    if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
      final Interpolator1D<T> extrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator<T>(interpolator, extrapolator);
    }
    final Interpolator1D<T> leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
    final Interpolator1D<T> rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator<T>(interpolator, leftExtrapolator, rightExtrapolator);
  }

  private static <T extends Interpolator1DDataBundle> Interpolator1D<T> getExtrapolator(final String extrapolatorName, final Interpolator1D<T> interpolator) {
    if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
      return new FlatExtrapolator1D<T>();
    } else if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
      return new LinearExtrapolator1D<T>(interpolator);
    }
    throw new IllegalArgumentException("Cannot get extrapolator " + extrapolatorName);
  }
}
