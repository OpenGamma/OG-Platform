/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaData;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class HestonFitter1Test {
  protected Logger _logger = LoggerFactory.getLogger(HestonFitter1Test.class);
  protected int _hotspotWarmupCycles = 1;
  protected int _benchmarkCycles = 0;

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.36;

  private static final double KAPPA = 1.4; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = 1.5 * THETA; // start level
  private static final double OMEGA = 0.25; // vol-of-vol
  private static final double RH0 = -0.7; // correlation
  private static final double EPS = 1e-6;

  private static final int N = 7;
  private static final double[] STRIKES;
  private static final double[] VOLS;
  private static final double[] SABR_VOLS;
  private static final double[] ERRORS;

  static {
    final CharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0, T);
    final FourierPricer pricer = new FourierPricer();
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final BlackImpliedVolatilityFormula blackImpliedVol = new BlackImpliedVolatilityFormula(new VanWijngaardenDekkerBrentSingleRootFinder());
    final double beta = 0.5;
    final double alpha = SIGMA * Math.pow(FORWARD, 1 - beta);
    final double nu = 0.4;
    final double rho = -0.65;

    STRIKES = new double[N];
    VOLS = new double[N];
    SABR_VOLS = new double[N];
    ERRORS = new double[N];

    for (int i = 0; i < N; i++) {
      ERRORS[i] = 0.001; //10bps errors 
      STRIKES[i] = 0.01 + 0.01 * i;
      final double price = pricer.price(FORWARD, STRIKES[i], 1.0, true, heston, -0.5, 1e-9, SIGMA);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES[i], T, true);
      VOLS[i] = blackImpliedVol.getImpliedVolatility(new BlackFunctionData(FORWARD, 1, SIGMA), option, price);
      SABR_VOLS[i] = sabr.getVolatilityFunction(option).evaluate(new SABRFormulaData(FORWARD, alpha, beta, nu, rho));
    }
  }

  @Test
  public void testExactFit() {
    final HestonFitter fitter = new HestonFitter();
    final double[] temp = new double[] {1.0, 0.04, VOL0, 0.2, 0.0};

    final BitSet fixed = new BitSet();
    fixed.set(2);

    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      final LeastSquareResults results = fitter.solve(FORWARD, T, STRIKES, VOLS, ERRORS, temp, fixed);
      assertEquals(0.0, results.getChiSq(), 2e-3);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on testExactFit", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        final LeastSquareResults results = fitter.solve(FORWARD, T, STRIKES, VOLS, ERRORS, temp, fixed);
        assertEquals(0.0, results.getChiSq(), 2e-3);
      }
      timer.finished();
    }

  }

  @Test
  public void testSABRFit() {
    final HestonFitter fitter = new HestonFitter();
    final double[] temp = new double[] {1.0, 0.1, 0.2, 0.3, -0.5};

    final BitSet fixed = new BitSet();
    final LeastSquareResults results = fitter.solve(FORWARD, T, STRIKES, SABR_VOLS, ERRORS, temp, fixed);
    assertTrue(results.getChiSq() < N * 100);

    //    System.out.println("chiSq: "+results.getChiSq());
    //    System.out.println("parameters: "+results.getParameters());
    //  
    //
    //    for(int i=0;i<N;i++){
    //      System.out.println(STRIKES[i]+"\t"+SABR_VOLS[i]);
    //    }
    //    System.out.println();
    //    
    //    DoubleMatrix1D parms = results.getParameters();
    //    CharacteristicExponent heston = new HestonCharacteristicExponent(parms.getEntry(0), parms.getEntry(1), parms.getEntry(2), parms.getEntry(3), parms.getEntry(4), T);
    //    FFTPricer pricer = new FFTPricer();
    //    double[][] pns = pricer.price(FORWARD, DF, true, heston, 0.01,  0.08, 20, -0.5, 1e-8, SIGMA);
    //    int n = pns.length;
    //    for(int i = 0; i < n;i++){
    //      double vol = BlackImpliedVolFormula.impliedVolNewton(pns[i][1], FORWARD, pns[i][0], DF, T, true);
    //      System.out.println(pns[i][0]+"\t"+vol);
    //    }
    //      
  }

  @Test
  public void testExactFitPrices() {
    final HestonFitter fitter = new HestonFitter();
    final double[] temp = new double[] {1.0, 0.04, VOL0, 0.2, 0.0};
    final double[] pErrors = new double[N];
    for (int i = 0; i < N; i++) {
      pErrors[i] = ERRORS[i] * BlackFormula.vega(FORWARD, STRIKES[i], DF, VOLS[i], T);
    }

    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      final LeastSquareResults results = fitter.solvePrice(FORWARD, T, STRIKES, VOLS, pErrors, temp, new BitSet());
      assertEquals(0.0, results.getChiSq(), 1e+1);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on FFT (price)", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        final LeastSquareResults results = fitter.solvePrice(FORWARD, T, STRIKES, VOLS, pErrors, temp, new BitSet());
        assertEquals(0.0, results.getChiSq(), 1e+1);
      }
      timer.finished();
    }

  }

  @Test
  public void testExactFitIntegral() {
    final HestonFitter fitter = new HestonFitter();
    final double[] temp = new double[] {1.0, 0.04, VOL0, 0.2, 0.0};
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      final LeastSquareResults results = fitter.solveFourierIntegral(FORWARD, T, STRIKES, VOLS, ERRORS, temp, new BitSet());
      assertEquals(0.0, results.getChiSq(), 1e-3);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on Fourier", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        final LeastSquareResults results = fitter.solveFourierIntegral(FORWARD, T, STRIKES, VOLS, ERRORS, temp, new BitSet());
        assertEquals(0.0, results.getChiSq(), 1e-3);
      }
      timer.finished();
    }

  }

}
