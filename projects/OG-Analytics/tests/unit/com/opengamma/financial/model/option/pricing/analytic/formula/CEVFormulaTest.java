/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.junit.Test;

/**
 * 
 */
public class CEVFormulaTest {

  @Test
  public void betaTest() {
    double f = 4;
    double k = 4.5;
    double atmVol = 0.3;
    double t = 2.5;
    double beta;

    for (int i = 0; i < 200; i++) {
      beta = i / 100.0;
      double sigma = atmVol * Math.pow(f, 1 - beta);
      double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);
      double vol;
      try {
        vol = BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
      } catch (Exception e) {
        vol = 0.0;
      }
      double sabrVol = SABRFormula.impliedVolitility(f, sigma, beta, 0.0, 0.0, k, t);
      System.out.println(beta + "\t" + vol + "\t" + sabrVol);
    }
  }
}
