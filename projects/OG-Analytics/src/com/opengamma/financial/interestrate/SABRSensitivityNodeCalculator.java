/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.surface.SurfaceValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For a set of SABR sensitivities, computes the sensitivities with respect to the node points of a given set of parameters interpolated surfaces.
 */
public class SABRSensitivityNodeCalculator {

  /**
   * Calculate the node sensitivities from existing sensitivities and a set of parameters with node points.
   * @param sensitivities The sensitivities. 
   * @param parameters The SABR parameters.
   * @return The node sensitivities.
   */
  PresentValueSABRSensitivityDataBundle calculateNodeSensitivities(final PresentValueSABRSensitivityDataBundle sensitivities, final SABRInterestRateParameters parameters) {
    @SuppressWarnings("unchecked")
    Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) parameters.getAlphaSurface().getInterpolatorData();
    SurfaceValue alphaNode = new SurfaceValue();
    for (Entry<DoublesPair, Double> entry : sensitivities.getAlpha().getMap().entrySet()) {
      Map<DoublesPair, Double> weight = parameters.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, entry.getKey());
      alphaNode = SurfaceValue.plus(alphaNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    @SuppressWarnings("unchecked")
    Map<Double, Interpolator1DDataBundle> rhoData = (Map<Double, Interpolator1DDataBundle>) parameters.getRhoSurface().getInterpolatorData();
    SurfaceValue rhoNode = new SurfaceValue();
    for (Entry<DoublesPair, Double> entry : sensitivities.getRho().getMap().entrySet()) {
      Map<DoublesPair, Double> weight = parameters.getRhoSurface().getInterpolator().getNodeSensitivitiesForValue(rhoData, entry.getKey());
      rhoNode = SurfaceValue.plus(rhoNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    @SuppressWarnings("unchecked")
    Map<Double, Interpolator1DDataBundle> nuData = (Map<Double, Interpolator1DDataBundle>) parameters.getNuSurface().getInterpolatorData();
    SurfaceValue nuNode = new SurfaceValue();
    for (Entry<DoublesPair, Double> entry : sensitivities.getNu().getMap().entrySet()) {
      Map<DoublesPair, Double> weight = parameters.getNuSurface().getInterpolator().getNodeSensitivitiesForValue(nuData, entry.getKey());
      nuNode = SurfaceValue.plus(nuNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    return new PresentValueSABRSensitivityDataBundle(alphaNode, rhoNode, nuNode);
  }

}
