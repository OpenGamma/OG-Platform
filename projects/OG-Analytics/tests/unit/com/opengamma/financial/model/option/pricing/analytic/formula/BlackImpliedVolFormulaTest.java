/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.junit.Assert.*;



import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class BlackImpliedVolFormulaTest {

  protected Logger _logger = LoggerFactory.getLogger(BlackImpliedVolFormulaTest.class);
  protected int _hotspotWarmupCycles = 200;
  protected int _benchmarkCycles = 1000;

  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final int N = 10;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = 50 + 2 * i;
      PRICES[i] = BlackFormula.optionPrice(FORWARD, STRIKES[i], DF, SIGMA, T, true);
    }
  }

  @Test
  public void testBrent() {

    for (int j = 0; j < _hotspotWarmupCycles; j++) {
      for (int i = 0; i < N; i++) {
        double vol = BlackImpliedVolFormula.impliedVol(PRICES[i], FORWARD, STRIKES[i], DF, T, true);
        assertEquals(SIGMA, vol, 1e-6);
      }
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on Brent", _benchmarkCycles);
      for (int j = 0; j < _benchmarkCycles; j++) {
        for (int i = 0; i < N; i++) {
          double vol = BlackImpliedVolFormula.impliedVol(PRICES[i], FORWARD, STRIKES[i], DF, T, true);
          assertEquals(SIGMA, vol, 1e-6);
        }     
      }
      timer.finished();
    }
  }

  @Test
  public void testNR() {
    for (int j = 0; j < _hotspotWarmupCycles; j++) {
      for (int i = 0; i < N; i++) {
        double vol = BlackImpliedVolFormula.impliedVolNewton(PRICES[i], FORWARD, STRIKES[i], DF, T, true);
        assertEquals(SIGMA, vol, 1e-6);
      }
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on Newton", _benchmarkCycles);
      for (int j = 0; j < _benchmarkCycles; j++) {
        for (int i = 0; i < N; i++) {
          double vol = BlackImpliedVolFormula.impliedVolNewton(PRICES[i], FORWARD, STRIKES[i], DF, T, true);
          assertEquals(SIGMA, vol, 1e-6);
        }     
      }
      timer.finished();
    }
  }

}
