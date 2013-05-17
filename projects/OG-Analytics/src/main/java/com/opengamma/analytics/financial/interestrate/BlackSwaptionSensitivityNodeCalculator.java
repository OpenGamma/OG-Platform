/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For a set of SABR sensitivities, computes the sensitivities with respect to the node points of a given set of parameters interpolated surfaces.
 */
public class BlackSwaptionSensitivityNodeCalculator {

  /**
   * Calculate the node sensitivities from existing sensitivities and a set of parameters with node points.
   * @param sensitivities The sensitivities. 
   * @param parameters The Black volatility parameters.
   * @return The node sensitivities.
   */
  public PresentValueBlackSwaptionSensitivity calculateNodeSensitivities(final PresentValueBlackSwaptionSensitivity sensitivities, final BlackFlatSwaptionParameters parameters) {
    Validate.isTrue(parameters.getGeneratorSwap().equals(sensitivities.getGeneratorSwap()), "Sensitivities and parameters should refer to the same swap generator");
    ArgumentChecker.isTrue(parameters.getVolatilitySurface() instanceof InterpolatedDoublesSurface, "Can only calculate node sensitivities for interpolated double surfaces");
    final InterpolatedDoublesSurface interpolatedSurface = (InterpolatedDoublesSurface) parameters.getVolatilitySurface();
    final Map<Double, Interpolator1DDataBundle> volatilityData = (Map<Double, Interpolator1DDataBundle>) interpolatedSurface.getInterpolatorData();
    SurfaceValue volatilityNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getSensitivity().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = interpolatedSurface.getInterpolator().getNodeSensitivitiesForValue(volatilityData, entry.getKey());
      volatilityNode = SurfaceValue.plus(volatilityNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    return new PresentValueBlackSwaptionSensitivity(volatilityNode, parameters.getGeneratorSwap());
  }

}
