/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class CEVFormulaTest {

  /**
   * For short dated options should have good agreement with the SABR formula for nu = 0
   */
  @Test
  public void betaTest() {
    double f = 4;
    double k = 3.5;
    double atmVol = 0.3;
    double t = 0.1;
    double beta;

    SABRFormula sabr = new SABRFormulaHagan();

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
      double sabrVol = sabr.impliedVolitility(f, sigma, beta, 0.0, 0.0, k, t);
      assertEquals(sabrVol, vol, 1e-5);
    }
  }

  @Test
  public void StrikeTest() {
    double f = 4;
    double k;
    double atmVol = 0.3;
    double t = 0.5;
    double beta = 0.5;
    double sigma = atmVol * Math.pow(f, 1 - beta);

    SABRFormula sabr = new SABRFormulaHagan();

    for (int i = 0; i < 20; i++) {
      k = 1.0 + i / 2.5;
      double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);
      double vol;
      try {
        vol = BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
      } catch (Exception e) {
        vol = 0.0;
      }
      double sabrVol = sabr.impliedVolitility(f, sigma, beta, 0.0, 0.0, k, t);
      assertEquals(sabrVol, vol, 1e-4);
    }
  }

  @Test
  public void StrikeAndBetaTest() {
    double f = 4;
    double k;
    double atmVol = 0.3;
    double t = 0.1;
    double beta;
    double sigma;

    SABRFormula sabr = new SABRFormulaHagan();

    for (int i = 0; i < 20; i++) {
      beta = (i + 1) / 20.0;
      sigma = atmVol * Math.pow(f, 1 - beta);
      for (int j = 0; j < 20; j++) {
        k = 3.0 + j / 10.0;
        double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);
        double vol;
        try {
          vol = BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
        } catch (Exception e) {
          vol = 0.0;
        }
        double sabrVol = sabr.impliedVolitility(f, sigma, beta, 0.0, 0.0, k, t);
        // System.out.println(beta + "\t" + k + "\t" + vol + "\t" + sabrVol);
        assertEquals(sabrVol, vol, 1e-5);
      }
    }
  }

}
