/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class TwoStateMarkovChainLocalVolCalculator {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle> GRID_INTERPOLATOR2D = new GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle>(
      INTERPOLATOR_1D,
      INTERPOLATOR_1D);

  public LocalVolatilitySurface calc(PDEFullResults1D[] denRes, TwoStateMarkovChainDataBundle chainData, LocalVolatilitySurface lvOverlay) {

    Map<DoublesPair, Double> lv = getLocalVolMap(denRes, chainData, lvOverlay);
    final Map<Double, Interpolator1DDoubleQuadraticDataBundle> interpolatorDB = GRID_INTERPOLATOR2D.getDataBundle(lv);

    Function<Double, Double> lvFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        return GRID_INTERPOLATOR2D.interpolate(interpolatorDB, new DoublesPair(ts[0], ts[1]));
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(lvFunc));
  }

  private Map<DoublesPair, Double> getLocalVolMap(PDEFullResults1D[] denRes, TwoStateMarkovChainDataBundle chainData, LocalVolatilitySurface lvOverlay) {
    int tNodes = denRes[0].getNumberTimeNodes();
    int xNodes = denRes[0].getNumberSpaceNodes();
    Map<DoublesPair, Double> lv = new HashMap<DoublesPair, Double>(tNodes * xNodes);
    double s;
    for (int j = 0; j < xNodes; j++) {
      s = denRes[0].getSpaceValue(j);
      double nu1 = chainData.getVol1() * chainData.getVol1() * Math.pow(s, 2 * chainData.getBeta1() - 2.0);
      double nu2 = chainData.getVol2() * chainData.getVol2() * Math.pow(s, 2 * chainData.getBeta2() - 2.0);

      for (int i = 0; i < tNodes; i++) {
        double t = denRes[0].getTimeValue(i);
        //form the equivalent local vol
        double p1 = denRes[0].getFunctionValue(j, i);
        double p2 = denRes[1].getFunctionValue(j, i);
        double p = p1 + p2;
        if (p > 0.0 && p1 >= 0.0 && p2 >= 0.0) { //if p = 0 can't find equivalent local vol for this t-s, so don't use point 
          double ol = 1.0;
          if (lvOverlay != null) {
            ol = lvOverlay.getVolatility(t, s);
          }
          lv.put(new DoublesPair(t, s), ol * Math.sqrt((nu1 * p1 + nu2 * p2) / p));
        }
      }
    }
    return lv;
  }
}
