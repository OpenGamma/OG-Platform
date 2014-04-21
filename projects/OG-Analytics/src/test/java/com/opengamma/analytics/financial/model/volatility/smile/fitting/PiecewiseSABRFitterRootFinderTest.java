/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRFitterRootFinder;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PiecewiseSABRFitterRootFinderTest {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };
  private static final PiecewiseSABRFitterRootFinder FITTER = new PiecewiseSABRFitterRootFinder();

  @Test
  (enabled = false)
  public void test() {

    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  (enabled = false)
  public void bumpTest() {
    final double bump = 5e-3;
    final int index = 1;
    final double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;

    final SABRFormulaData[] parms = FITTER.getFittedfModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    for (final SABRFormulaData parm : parms) {
      System.out.println(parm.toString());
    }
    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  (enabled = false)
  public void flatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);

    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(0.2, vol, 1e-9);
    }
  }



}
