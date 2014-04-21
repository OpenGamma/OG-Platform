/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.volatility.local.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class TwoStateMarkovChainLocalVolCalculator {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  public AbsoluteLocalVolatilitySurface calc(final PDEFullResults1D[] denRes, final TwoStateMarkovChainDataBundle chainData, final AbsoluteLocalVolatilitySurface lvOverlay) {

    final Map<DoublesPair, Double> lv = getLocalVolMap(denRes, chainData, lvOverlay);
    final Map<Double, Interpolator1DDataBundle> interpolatorDB = GRID_INTERPOLATOR2D.getDataBundle(lv);

    final Function<Double, Double> lvFunc = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        return GRID_INTERPOLATOR2D.interpolate(interpolatorDB, DoublesPair.of(ts[0].doubleValue(), ts[1].doubleValue()));
      }
    };

    return new AbsoluteLocalVolatilitySurface(FunctionalDoublesSurface.from(lvFunc));
  }

  private Map<DoublesPair, Double> getLocalVolMap(final PDEFullResults1D[] denRes, final TwoStateMarkovChainDataBundle chainData, final AbsoluteLocalVolatilitySurface lvOverlay) {
    final int tNodes = denRes[0].getNumberTimeNodes();
    final int xNodes = denRes[0].getNumberSpaceNodes();
    final Map<DoublesPair, Double> lv = new HashMap<>(tNodes * xNodes);
    double s;
    for (int j = 0; j < xNodes; j++) {
      s = denRes[0].getSpaceValue(j);
      final double nu1 = chainData.getVol1() * chainData.getVol1() * Math.pow(s, 2 * chainData.getBeta1());
      final double nu2 = chainData.getVol2() * chainData.getVol2() * Math.pow(s, 2 * chainData.getBeta2());

      for (int i = 0; i < tNodes; i++) {
        final double t = denRes[0].getTimeValue(i);
        //form the equivalent local vol
        final double p1 = denRes[0].getFunctionValue(j, i);
        final double p2 = denRes[1].getFunctionValue(j, i);
        final double p = p1 + p2;
        if (p > 0.0 && p1 >= 0.0 && p2 >= 0.0) { //if p = 0 can't find equivalent local vol for this t-s, so don't use point
          double ol = 1.0;
          if (lvOverlay != null) {
            ol = lvOverlay.getVolatility(t, s);
          }
          lv.put(DoublesPair.of(t, s), ol * Math.sqrt((nu1 * p1 + nu2 * p2) / p));
        }
      }
    }
    return lv;
  }
}
