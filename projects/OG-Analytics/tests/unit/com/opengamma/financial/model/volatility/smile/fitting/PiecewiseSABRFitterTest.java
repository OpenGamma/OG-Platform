/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

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
    PiecewiseSABRFitter fitter = new PiecewiseSABRFitter(FORWARD, STRIKES, EXPIRY, VOLS);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      double k = 700 + 1300 * i / 199.;
      double vol = fitter.getVol(k);
      System.out.println(k + "\t" + vol);
    }
  }

}
