/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingSetup;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MultiCapFloorPricer;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class MultiCapFloorPricerTest extends CapletStrippingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiCapFloorPricerTest.class);

  private static VolatilitySurface VOL;

  static {
    final Function2D<Double, Double> vol = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double k) {
        return 0.3 + 0.8 * Math.exp(-0.3 * t);
      }
    };
    VOL = new VolatilitySurface(FunctionalDoublesSurface.from(vol));
  }

  @Test
  public void priceTest() {
    final MulticurveProviderDiscount yieldCurve = getYieldCurves();

    // for each strike make a set of caps at that strike
    final int nStrikes = getNumberOfStrikes();
    for (int i = 0; i < nStrikes; i++) {
      final List<CapFloor> caps = getCaps(i);
      final int n = caps.size();
      final MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
      final double[] prices = multiPricer.price(VOL);
      assertEquals("wrong number of prices", n, prices.length);
      for (int j = 0; j < n; j++) {
        final CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
        final double p = pricer.price(VOL);
        assertEquals(p, prices[j], 1e-13);
      }
    }
  }

  @Test
  public void impVolTest() {
    final MulticurveProviderDiscount yieldCurve = getYieldCurves();

    // for each strike make a set of caps at that strike
    final int nStrikes = getNumberOfStrikes();
    for (int i = 0; i < nStrikes; i++) {
      final List<CapFloor> caps = getCaps(i);
      final int n = caps.size();
      final MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
      final double[] vols = multiPricer.impliedVols(VOL);
      assertEquals("wrong number of prices", n, vols.length);
      for (int j = 0; j < n; j++) {
        final CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
        final double v = pricer.impliedVol(VOL);
        assertEquals(v, vols[j], 1e-9);
      }
    }
  }

  @Test
  public void vegaTest() {

    final double eps = 1e-5;

    final MulticurveProviderDiscount yieldCurve = getYieldCurves();
    final int nStrikes = getNumberOfStrikes();
    for (int strikeIndex = 0; strikeIndex < nStrikes; strikeIndex++) {
      final List<CapFloor> caps = getCaps(strikeIndex);
      final int n = caps.size();
      final MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
      final int m = multiPricer.getTotalNumberOfCaplets();
      final double[] capletVols = new double[m];
      Arrays.fill(capletVols, 0.5);

      final double[] capletPrices = multiPricer.priceFromCapletVols(capletVols);
      // calculate vega by finite difference
      final double[][] fdVega = new double[n][m];
      for (int j = 0; j < m; j++) {
        final double temp = capletVols[j];
        capletVols[j] += eps;
        final double[] up = multiPricer.priceFromCapletVols(capletVols);
        capletVols[j] -= 2 * eps;
        final double[] down = multiPricer.priceFromCapletVols(capletVols);
        capletVols[j] = temp;
        for (int i = 0; i < n; i++) {
          fdVega[i][j] = (up[i] - down[i]) / 2 / eps;
        }
      }

      final double[][] vega = multiPricer.vegaFromCapletVols(capletVols).getData();
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
          assertEquals(i + "\t" + j, fdVega[i][j], vega[i][j], Math.max(1e-12, 1e-8 * capletPrices[i]));
        }
      }

    }
  }

  @SuppressWarnings("unused")
  @Test
  public void priceTimeTest() {

    final int warmup = 1;
    final int benchmarkCycles = 0;

    final MulticurveProviderDiscount yieldCurve = getYieldCurves();

    final int nStrikes = getNumberOfStrikes();
    final MultiCapFloorPricer[] multiPricers = new MultiCapFloorPricer[nStrikes];
    final CapFloorPricer[][] pricers = new CapFloorPricer[nStrikes][];
    //  final List<List<CapFloor>> allCaps = new ArrayList<>(nStrikes);
    for (int i = 0; i < nStrikes; i++) {
      final List<CapFloor> caps = getCaps(i);
      multiPricers[i] = new MultiCapFloorPricer(caps, yieldCurve);
      final int n = caps.size();
      pricers[i] = new CapFloorPricer[n];
      for (int j = 0; j < n; j++) {
        pricers[i][j] = new CapFloorPricer(caps.get(j), yieldCurve);
      }
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        final double[] prices = multiPricers[i].price(VOL);
      }
    }

    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(LOGGER, "processing {} cycles on timeTest - multiPricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          final double[] prices = multiPricers[i].price(VOL);
        }
      }
      timer.finished();
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        final int n = pricers[i].length;
        for (int j = 0; j < n; j++) {
          final double p = pricers[i][j].price(VOL);
        }
      }
    }

    if (benchmarkCycles > 0) {
      final OperationTimer timer2 = new OperationTimer(LOGGER, "processing {} cycles on timeTest - single Pricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          final int n = pricers[i].length;
          for (int j = 0; j < n; j++) {
            final double p = pricers[i][j].price(VOL);
          }
        }
      }
      timer2.finished();
    }
  }

  // since finding implied vol is expensive compared to finding a price (10-20 times more), there is little (relative) difference here. This is why it is
  // quicker to to caplet stripping based on price (weighted by vega) rather than implied vol.
  @SuppressWarnings("unused")
  @Test
  public void volTimeTest() {

    final int warmup = 1;
    final int benchmarkCycles = 0;

    final MulticurveProviderDiscount yieldCurve = getYieldCurves();

    final int nStrikes = getNumberOfStrikes();
    final MultiCapFloorPricer[] multiPricers = new MultiCapFloorPricer[nStrikes];
    final CapFloorPricer[][] pricers = new CapFloorPricer[nStrikes][];
    final List<List<CapFloor>> allCaps = new ArrayList<>(nStrikes);
    for (int i = 0; i < nStrikes; i++) {
      final List<CapFloor> caps = getCaps(i);
      multiPricers[i] = new MultiCapFloorPricer(caps, yieldCurve);
      final int n = caps.size();
      pricers[i] = new CapFloorPricer[n];
      for (int j = 0; j < n; j++) {
        pricers[i][j] = new CapFloorPricer(caps.get(j), yieldCurve);
      }
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        final double[] vols = multiPricers[i].impliedVols(VOL);
      }
    }

    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(LOGGER, "processing {} cycles on timeTest - multiPricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          final double[] vols = multiPricers[i].impliedVols(VOL);
        }
      }
      timer.finished();
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        final int n = pricers[i].length;
        for (int j = 0; j < n; j++) {
          final double v = pricers[i][j].impliedVol(VOL);
        }
      }
    }

    if (benchmarkCycles > 0) {
      final OperationTimer timer2 = new OperationTimer(LOGGER, "processing {} cycles on timeTest - single Pricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          final int n = pricers[i].length;
          for (int j = 0; j < n; j++) {
            final double v = pricers[i][j].impliedVol(VOL);
          }
        }
      }
      timer2.finished();
    }
  }

}
