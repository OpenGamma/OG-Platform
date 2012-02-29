/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * 
 */
public class PiecewiseSABRFitterTest {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };

  @Test(enabled = false)
  public void test() {
    final PiecewiseSABRFitter fitter = new PiecewiseSABRFitter(FORWARD, STRIKES, EXPIRY, VOLS);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = fitter.getVol(k);
      System.out.println(vol);
    }
  }

  @Test
  public void FlatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    final PiecewiseSABRFitter fitter = new PiecewiseSABRFitter(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = fitter.getVol(k);
      assertEquals(0.2, vol, 1e-9);
    }
  }

}
