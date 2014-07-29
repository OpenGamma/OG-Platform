/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.util.amount.SurfaceValue;
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
  public static PresentValueSABRSensitivityDataBundle calculateNodeSensitivities(final PresentValueSABRSensitivityDataBundle sensitivities,
      final SABRInterestRateParameters parameters) {
    final Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) parameters.getAlphaSurface().getInterpolatorData();
    SurfaceValue alphaNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getAlpha().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = parameters.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, entry.getKey());
      alphaNode = SurfaceValue.plus(alphaNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    final Map<Double, Interpolator1DDataBundle> betaData = (Map<Double, Interpolator1DDataBundle>) parameters.getBetaSurface().getInterpolatorData();
    SurfaceValue betaNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getBeta().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = parameters.getBetaSurface().getInterpolator().getNodeSensitivitiesForValue(betaData, entry.getKey());
      betaNode = SurfaceValue.plus(betaNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    final Map<Double, Interpolator1DDataBundle> rhoData = (Map<Double, Interpolator1DDataBundle>) parameters.getRhoSurface().getInterpolatorData();
    SurfaceValue rhoNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getRho().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = parameters.getRhoSurface().getInterpolator().getNodeSensitivitiesForValue(rhoData, entry.getKey());
      rhoNode = SurfaceValue.plus(rhoNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    final Map<Double, Interpolator1DDataBundle> nuData = (Map<Double, Interpolator1DDataBundle>) parameters.getNuSurface().getInterpolatorData();
    SurfaceValue nuNode = new SurfaceValue();
    for (final Entry<DoublesPair, Double> entry : sensitivities.getNu().getMap().entrySet()) {
      final Map<DoublesPair, Double> weight = parameters.getNuSurface().getInterpolator().getNodeSensitivitiesForValue(nuData, entry.getKey());
      nuNode = SurfaceValue.plus(nuNode, SurfaceValue.multiplyBy(SurfaceValue.from(weight), entry.getValue()));
    }
    return new PresentValueSABRSensitivityDataBundle(alphaNode, betaNode, rhoNode, nuNode);
  }

}
