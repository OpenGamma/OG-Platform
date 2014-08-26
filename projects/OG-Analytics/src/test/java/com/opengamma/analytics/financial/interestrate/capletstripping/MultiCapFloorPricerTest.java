/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class MultiCapFloorPricerTest extends CapletStrippingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiCapFloorPricerTest.class);

  private static final VolatilitySurface s_VolSurface;

  static {
    Function2D<Double, Double> vol = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(Double t, Double k) {
        return 0.3 - 0.5 * k + 0.8 * Math.exp(-0.3 * t);
      }
    };
    s_VolSurface = new VolatilitySurface(FunctionalDoublesSurface.from(vol));
  }

  /**
   * Test prices from {@link MultiCapFloorPricer} against those from {@link CapFloorPricer} when presented with 
   * a {@link VolatilitySurface}
   */
  @Test
  public void volSurfacePriceTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();

    List<CapFloor> caps = getAllCaps();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] prices = multiPricer.price(s_VolSurface);
    assertEquals("wrong number of prices", n, prices.length);
    for (int j = 0; j < n; j++) {
      CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
      double p = pricer.price(s_VolSurface);
      assertEquals(p, prices[j], 1e-13);
    }
  }

  /**
   * Test prices from {@link MultiCapFloorPricer} against those from {@link CapFloorPricer} when presented with 
   * cap volatilities 
   */
  @Test
  public void capVolPriceTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();

    List<CapFloor> caps = getAllCaps();
    double[] capVols = getAllCapVols();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] prices = multiPricer.price(capVols);
    assertEquals("wrong number of prices", n, prices.length);
    for (int j = 0; j < n; j++) {
      CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
      double p = pricer.price(capVols[j]);
      assertEquals(p, prices[j], 1e-13);
    }
  }

  @Test
  public void volSurfaceImpVolTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();

    List<CapFloor> caps = getAllCaps();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] vols = multiPricer.impliedVols(s_VolSurface);
    assertEquals("wrong number of vols", n, vols.length);
    for (int j = 0; j < n; j++) {
      CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
      double v = pricer.impliedVol(s_VolSurface);
      assertEquals(v, vols[j], 1e-9);
    }
  }

  @Test
  public void vegaFromCapVolsTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    double[] capVols = getAllCapVols();
    List<CapFloor> caps = getAllCaps();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] vega = multiPricer.vega(capVols);
    assertEquals("wrong number of vegas", n, vega.length);
    for (int j = 0; j < n; j++) {
      CapFloorPricer pricer = new CapFloorPricer(caps.get(j), yieldCurve);
      double v = pricer.vega(capVols[j]);
      assertEquals(v, vega[j], 1e-9);
    }
  }

  /**
   * compute vega by finite difference and compare it too what is calculated by MultiCapFloorPricer
   */
  @Test
  public void vegaTest() {

    double eps = 1e-5;

    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    List<CapFloor> caps = getAllCaps();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    int m = multiPricer.getNumCaplets();
    double[] capletVols = new double[m];
    Arrays.fill(capletVols, 0.5);

    double[] capletPrices = multiPricer.priceFromCapletVols(capletVols);

    // calculate vega by finite difference
    double[][] fdVega = new double[n][m];
    for (int j = 0; j < m; j++) {
      double temp = capletVols[j];
      capletVols[j] += eps;
      double[] up = multiPricer.priceFromCapletVols(capletVols);
      capletVols[j] -= 2 * eps;
      double[] down = multiPricer.priceFromCapletVols(capletVols);
      capletVols[j] = temp;
      for (int i = 0; i < n; i++) {
        fdVega[i][j] = (up[i] - down[i]) / 2 / eps;
      }
    }

    double[][] vega = multiPricer.vegaFromCapletVols(capletVols).getData();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        assertEquals(i + "\t" + j, fdVega[i][j], vega[i][j], Math.max(1e-12, 1e-8 * capletPrices[i]));
      }
    }

  }

  /**
   * the cap vol-vega is the sensitivity of a cap's implied volatility to the volatility of its constituent caplets.
   * Here we calculate it by finite difference  (which takes a while as there are 109 caps and 823 unique caplets), 
   * and compare this against what {@link MultiCapFloorPricer} produces 
   */
  @Test
  public void capVolVegaTest() {

    double eps = 1e-5;

    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    List<CapFloor> caps = getAllCaps();
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    int m = multiPricer.getNumCaplets();
    double[] capletVols = new double[m];
    Arrays.fill(capletVols, 0.35);

    double[] capVols = multiPricer.impliedVols(multiPricer.priceFromCapletVols(capletVols));
    assertEquals(n, capVols.length);
    for (int i = 0; i < n; i++) {
      assertEquals("cap vol", 0.35, capVols[i], 1e-9);
    }

    DoubleMatrix2D capVolVega = multiPricer.capVolVega(capletVols);

    // calculate vega by finite difference
    DoubleMatrix2D fdVega = new DoubleMatrix2D(n, m);
    double[][] data = fdVega.getData();
    for (int j = 0; j < m; j++) {
      double temp = capletVols[j];
      capletVols[j] += eps;
      double[] up = multiPricer.impliedVols(multiPricer.priceFromCapletVols(capletVols));
      capletVols[j] -= 2 * eps;
      double[] down = multiPricer.impliedVols(multiPricer.priceFromCapletVols(capletVols));
      capletVols[j] = temp;
      for (int i = 0; i < n; i++) {
        data[i][j] = (up[i] - down[i]) / 2 / eps;
      }
    }

    //fdVega is a finite difference calculation inside which is a root-finding (for the implied vol), hence the tolerance 
    //is fairly low
    AssertMatrix.assertEqualsMatrix(fdVega, capVolVega, 5e-6);

  }

  @Test
  public void accessTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    List<CapFloor> caps = getCaps(0); // this includes the 10 year cap
    int n = caps.size();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    assertEquals(n, multiPricer.getNumCaps());
    assertEquals(39, multiPricer.getCapletExpiries().length); //4*10 - 1
    assertEquals(1, multiPricer.getStrikes().length);
    assertEquals(1, multiPricer.getCapStartTimes().length); //one common start time
    assertEquals(n, multiPricer.getCapEndTimes().length);
  }

  @Test
  public void capletFwdTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    List<CapFloor> caps = getATMCaps();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] exp = multiPricer.getCapletExpiries();
    double[] fwd = multiPricer.getCapletForwardRates();
    DoublesPair[] points = multiPricer.getExpiryStrikeArray();
    int nCaplets = multiPricer.getNumCaplets();
    int nExp = exp.length;

    assertEquals("fwd length", nExp, fwd.length);
    assertEquals("points length", nCaplets, points.length);

  }

  @Test
  public void capIntrTest() {
    MulticurveProviderDiscount yieldCurve = getYieldCurves();
    List<CapFloor> caps = getATMCaps();
    MultiCapFloorPricer multiPricer = new MultiCapFloorPricer(caps, yieldCurve);
    double[] intrVal = multiPricer.getIntrinsicCapValues();
    int nCaps = caps.size();
    assertEquals(nCaps, intrVal.length);
    for (int i = 0; i < nCaps; i++) {
      assertTrue(intrVal[i]>0.0); //Cap ATM means the strike equals the swap rate - individual caplets may be ITM 
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void priceTimeTest() {

    int warmup = 1;
    int benchmarkCycles = 0;

    MulticurveProviderDiscount yieldCurve = getYieldCurves();

    int nStrikes = getNumberOfStrikes();
    MultiCapFloorPricer[] multiPricers = new MultiCapFloorPricer[nStrikes];
    CapFloorPricer[][] pricers = new CapFloorPricer[nStrikes][];
    //   List<List<CapFloor>> allCaps = new ArrayList<>(nStrikes);
    for (int i = 0; i < nStrikes; i++) {
      List<CapFloor> caps = getCaps(i);
      multiPricers[i] = new MultiCapFloorPricer(caps, yieldCurve);
      int n = caps.size();
      pricers[i] = new CapFloorPricer[n];
      for (int j = 0; j < n; j++) {
        pricers[i][j] = new CapFloorPricer(caps.get(j), yieldCurve);
      }
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        double[] prices = multiPricers[i].price(s_VolSurface);
      }
    }

    if (benchmarkCycles > 0) {
      OperationTimer timer = new OperationTimer(LOGGER, "processing {} cycles on timeTest - multiPricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          double[] prices = multiPricers[i].price(s_VolSurface);
        }
      }
      timer.finished();
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        int n = pricers[i].length;
        for (int j = 0; j < n; j++) {
          double p = pricers[i][j].price(s_VolSurface);
        }
      }
    }

    if (benchmarkCycles > 0) {
      OperationTimer timer2 = new OperationTimer(LOGGER, "processing {} cycles on timeTest - single Pricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          int n = pricers[i].length;
          for (int j = 0; j < n; j++) {
            double p = pricers[i][j].price(s_VolSurface);
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

    int warmup = 1;
    int benchmarkCycles = 0;

    MulticurveProviderDiscount yieldCurve = getYieldCurves();

    int nStrikes = getNumberOfStrikes();
    MultiCapFloorPricer[] multiPricers = new MultiCapFloorPricer[nStrikes];
    CapFloorPricer[][] pricers = new CapFloorPricer[nStrikes][];
    List<List<CapFloor>> allCaps = new ArrayList<>(nStrikes);
    for (int i = 0; i < nStrikes; i++) {
      List<CapFloor> caps = getCaps(i);
      multiPricers[i] = new MultiCapFloorPricer(caps, yieldCurve);
      int n = caps.size();
      pricers[i] = new CapFloorPricer[n];
      for (int j = 0; j < n; j++) {
        pricers[i][j] = new CapFloorPricer(caps.get(j), yieldCurve);
      }
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        double[] vols = multiPricers[i].impliedVols(s_VolSurface);
      }
    }

    if (benchmarkCycles > 0) {
      OperationTimer timer = new OperationTimer(LOGGER, "processing {} cycles on timeTest - multiPricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          double[] vols = multiPricers[i].impliedVols(s_VolSurface);
        }
      }
      timer.finished();
    }

    for (int count = 0; count < warmup; count++) {
      for (int i = 0; i < nStrikes; i++) {
        int n = pricers[i].length;
        for (int j = 0; j < n; j++) {
          double v = pricers[i][j].impliedVol(s_VolSurface);
        }
      }
    }

    if (benchmarkCycles > 0) {
      OperationTimer timer2 = new OperationTimer(LOGGER, "processing {} cycles on timeTest - single Pricer", benchmarkCycles);
      for (int count = 0; count < benchmarkCycles; count++) {
        for (int i = 0; i < nStrikes; i++) {
          int n = pricers[i].length;
          for (int j = 0; j < n; j++) {
            double v = pricers[i][j].impliedVol(s_VolSurface);
          }
        }
      }
      timer2.finished();
    }
  }

}
