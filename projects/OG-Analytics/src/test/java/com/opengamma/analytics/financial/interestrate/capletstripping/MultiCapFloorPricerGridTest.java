/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class MultiCapFloorPricerGridTest extends CapletStrippingSetup {

  /**
   * The ATM caps have different strikes for each cap length, so the caplets do not lie on a regular expiry-strike grid 
   */
  @Test
  public void test() {
    List<CapFloor> caps = getATMCaps();
    int nCaps = caps.size();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, getYieldCurves());
    double[] strikes = pricer.getStrikes();
    double[] expiries = pricer.getCapletExpiries();
    int nStrikes = strikes.length;
    int nExp = expiries.length;

    assertEquals(nStrikes * nExp, pricer.getGridSize());
    assertTrue(pricer.getGridSize() > pricer.getNumCaplets());

    DoublesPair[] points = pricer.getExpiryStrikeArray();
    assertEquals(nStrikes * nExp, points.length);
    for (int i = 0; i < nStrikes; i++) {
      double k = strikes[i];
      for (int j = 0; j < nExp; j++) {
        DoublesPair p = points[i * nExp + j];
        assertEquals(expiries[j], p.first);
        assertEquals(k, p.second);
      }
    }

    double[] capletVols = new double[pricer.getGridSize()];
    Arrays.fill(capletVols, 0.45);
    double[] capPrices = pricer.priceFromCapletVols(capletVols);
    assertEquals(nCaps, capPrices.length);
    double[] iv = pricer.impliedVols(capPrices);
    for (double element : iv) {
      assertEquals(0.45, element, 1e-9);
    }

    DoubleMatrix2D capVega = pricer.vegaFromCapletVols(capletVols);
    assertEquals(nCaps, capVega.getNumberOfRows());
    assertEquals(pricer.getGridSize(), capVega.getNumberOfColumns());
  }

  /**
   * the absolute strike caps have caplets that DO lie on a regular expiry-strike grid 
   */
  @Test
  public void regGridTest() {
    List<CapFloor> caps = getAllCapsExATM();
    MultiCapFloorPricerGrid gridPricer = new MultiCapFloorPricerGrid(caps, getYieldCurves());
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, getYieldCurves());
    double[] strikes = gridPricer.getStrikes();
    double[] expiries = gridPricer.getCapletExpiries();
    int nStrikes = strikes.length;
    int nExp = expiries.length;

    assertEquals(nStrikes * nExp, gridPricer.getGridSize());
    assertEquals(gridPricer.getGridSize(), gridPricer.getNumCaplets());

    double[] capletVols = new double[pricer.getNumCaplets()];
    Arrays.fill(capletVols, 0.35);

    DoubleMatrix2D vega1 = pricer.vegaFromCapletVols(capletVols);
    DoubleMatrix2D vega2 = gridPricer.vegaFromCapletVols(capletVols);
    AssertMatrix.assertEqualsMatrix(vega1, vega2, 1e-14);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongNumCapletVolsPriceTest() {
    List<CapFloor> caps = getATMCaps();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, getYieldCurves());
    double[] capletVols = new double[pricer.getNumCaplets()];
    Arrays.fill(capletVols, 0.35);
    pricer.priceFromCapletVols(capletVols);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongNumCapletVolsVegaTest() {
    List<CapFloor> caps = getATMCaps();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, getYieldCurves());
    double[] capletVols = new double[pricer.getNumCaplets()];
    Arrays.fill(capletVols, 0.35);
    pricer.vegaFromCapletVols(capletVols);
  }
}
