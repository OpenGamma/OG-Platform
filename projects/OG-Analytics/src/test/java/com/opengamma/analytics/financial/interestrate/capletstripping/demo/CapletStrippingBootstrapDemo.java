/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.demo;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingBootstrap;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * In this demo, we fit caps at each absolute strike in turn using a bootstrap.
 * These separate solutions are joined together to form a volatility surface; the output is this surface
 * sampled on a grid (101 by 101), such that it can be plotted as an Excel surface plot (or imported into some other
 * visualisation tool).
 */
public class CapletStrippingBootstrapDemo extends CapletStrippingSetup {

  @Test
  public void test() {

    final int nSamples = 101;
    final MulticurveProviderDiscount yieldCurve = getYieldCurves();
    final int n = getNumberOfStrikes();

    final double[][] curve = new double[nSamples][n];

    for (int i = 0; i < n; i++) {
      final List<CapFloor> caps = getCaps(i);
      final double[] capVols = getCapVols(i);
      final CapletStrippingBootstrap bootstrap = new CapletStrippingBootstrap(caps, yieldCurve);
      final double[] capletVols = bootstrap.capletVolsFromCapVols(capVols);
      final VolatilitySurface volCurve = getPiecewise(capletVols, bootstrap.getEndTimes());
      for (int index = 0; index < nSamples; index++) {
        final double t = index * 10.0 / (nSamples - 1);
        curve[index][i] = volCurve.getVolatility(t, getStrikes()[i]);
      }

    }

    System.out.print("\n");
    final double[] strikes = getStrikes();
    for (int i = 0; i < n; i++) {
      System.out.print("\t" + strikes[i]);
    }
    System.out.print("\n");
    for (int index = 0; index < nSamples; index++) {
      final double t = index * 10.0 / (nSamples - 1);
      System.out.print(t);
      for (int i = 0; i < n; i++) {
        System.out.print("\t" + curve[index][i]);
      }
      System.out.print("\n");
    }
  }

  private VolatilitySurface getPiecewise(final double[] capletVols, final double[] endTimes) {
    final int n = capletVols.length;
    final Function2D<Double, Double> func = new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double t, final Double k) {
        final int index = Arrays.binarySearch(endTimes, t);
        if (index >= 0) {
          if (index >= (n - 1)) {
            return capletVols[n - 1];
          }
          return capletVols[index + 1];
        } else if (index == -(n + 1)) {
          return capletVols[n - 1];
        } else {
          return capletVols[-index - 1];
        }
      }
    };

    return new VolatilitySurface(FunctionalDoublesSurface.from(func));
  }

}
