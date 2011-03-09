/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.finiteDifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CEVFormulaTest {

  /**
   * For short dated options should have good agreement with the SABR formula for nu = 0
   */
  @Test
  public void betaTest() {
    final double f = 4;
    final double k = 3.5;
    final double atmVol = 0.3;
    final double t = 0.1;
    double beta;

    final SABRFormula sabr = new SABRFormulaHagan();

    for (int i = 0; i < 200; i++) {
      beta = i / 100.0;
      final double sigma = atmVol * Math.pow(f, 1 - beta);
      final double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);

      double vol;
      try {
        vol = BlackImpliedVolFormula.impliedVolNewton(price, f, k, 1.0, t, true);
      } catch (final Exception e) {
        vol = 0.0;
      }
      final double sabrVol = sabr.impliedVolatility(f, sigma, beta, 0.0, 0.0, k, t);
      assertEquals(sabrVol, vol, 1e-5);
    }
  }

  @Test
  public void StrikeTest() {
    final double f = 4;
    double k;
    final double atmVol = 0.3;
    final double t = 0.5;
    final double beta = 0.5;
    final double sigma = atmVol * Math.pow(f, 1 - beta);

    final SABRFormula sabr = new SABRFormulaHagan();

    for (int i = 0; i < 20; i++) {
      k = 1.0 + i / 2.5;
      final double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);
      double vol;
      try {
        vol = BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
      } catch (final Exception e) {
        vol = 0.0;
      }
      final double sabrVol = sabr.impliedVolatility(f, sigma, beta, 0.0, 0.0, k, t);
      assertEquals(sabrVol, vol, 1e-4);
    }
  }

  @Test
  public void StrikeAndBetaTest() {
    final double f = 4;
    double k;
    final double atmVol = 0.3;
    final double t = 0.1;
    double beta;
    double sigma;

    final SABRFormula sabr = new SABRFormulaHagan();

    for (int i = 0; i < 20; i++) {
      beta = (i + 1) / 20.0;
      sigma = atmVol * Math.pow(f, 1 - beta);
      for (int j = 0; j < 20; j++) {
        k = 3.0 + j / 10.0;
        final double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigma, t, true);
        double vol;
        try {
          vol = BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
        } catch (final Exception e) {
          vol = 0.0;
        }
        final double sabrVol = sabr.impliedVolatility(f, sigma, beta, 0.0, 0.0, k, t);
        // System.out.println(beta + "\t" + k + "\t" + vol + "\t" + sabrVol);
        assertEquals(sabrVol, vol, 1e-5);
      }
    }
  }
  
  
  @Test
  public void funnySmileTest(){
  
    final double beta = 0.4;
    final double t = 5.0;
    final double r = 0.0;
    final double spot = 100;
    final double k = spot*Math.exp(-r*t);
   
    final double atmVol = 0.20;
    final double volBeta = atmVol*Math.pow(k,1-beta);
    
    for(int i=0;i<101;i++){
      double f = 350.0 + 1.0*i;
      double cevPrice = CEVFormula.optionPrice(f,k,beta,1.0,volBeta,t, false);
      double cevVol = BlackImpliedVolFormula.impliedVol(cevPrice, f,k,1.0,t, false);
      
       System.out.println(f +"\t"+cevPrice+"\t"+cevVol);
    }
   
    
  
  }

}
