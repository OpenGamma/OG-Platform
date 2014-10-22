/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.provider.description.interestrate.SwaptionSurfaceProvider;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For a set of surface swaption sensitivities, computes the sensitivities with respect to the node points of a 
 * given set of parameters interpolated surface.
 */
public class SwaptionSurfaceSensitivityNodeCalculator {

  /**
   * Computes the sensitivity to the nodes of the parameter surface from sensitivities to intermediary points 
   * and the parameter surface description.
   * @param sensitivities The sensitivity to intermediary points.
   * @param parameters The surface parameters. The underlying volatility surface should be of type InterpolatedDoublesSurface.
   * @return The sensitivity to the nodes.
   */
  public PresentValueSwaptionSurfaceSensitivity calculateNodeSensitivities(
      final PresentValueSwaptionSurfaceSensitivity sensitivities, final SwaptionSurfaceProvider parameters) {
    ArgumentChecker.notNull(sensitivities, "Black swaption sensitivities");
    ArgumentChecker.notNull(parameters, "Black swaption parameters");
    ArgumentChecker.isTrue(parameters.getGeneratorSwap().equals(sensitivities.getGeneratorSwap()), 
        "Sensitivities and parameters should refer to the same swap generator");
    ArgumentChecker.isTrue(parameters.getParameterSurface() instanceof InterpolatedDoublesSurface, 
        "Can only calculate node sensitivities for interpolated double surfaces");
    InterpolatedDoublesSurface interpolatedSurface = (InterpolatedDoublesSurface) parameters.getParameterSurface();
    Map<Double, Interpolator1DDataBundle> volatilityData = 
        (Map<Double, Interpolator1DDataBundle>) interpolatedSurface.getInterpolatorData();
    Interpolator2D interpolator = interpolatedSurface.getInterpolator();
    SurfaceValue sensitivityNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getSensitivity().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = interpolator.getNodeSensitivitiesForValue(volatilityData, entry.getKey());
      sensitivityNode = SurfaceValue.plus(sensitivityNode, 
          SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    return new PresentValueSwaptionSurfaceSensitivity(sensitivityNode, parameters.getGeneratorSwap());
  }

}
