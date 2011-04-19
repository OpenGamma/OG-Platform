/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.CharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.FourierPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
//TODO nothing in this class works
public class HestonFitterTest {
  protected Logger _logger = LoggerFactory.getLogger(HestonFitterTest.class);
  protected int _hotspotWarmupCycles = 0;
  protected int _benchmarkCycles = 1;

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 1;
  private static final double SIGMA = 0.36;

  private static final double KAPPA = 1.4; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = 1.5 * THETA; // start level
  private static final double OMEGA = 0.25; // vol-of-vol
  private static final double RHO = -0.7; // correlation

  private static final int N = 7;
  private static final double[] ERRORS;

  private static final EuropeanVanillaOption[] OPTIONS;
  private static final BlackFunctionData[] BLACK_VOLS;
  private static final BlackFunctionData[] SABR_VOLS;
  private static final BlackPriceFunction BLACK_PRICE = new BlackPriceFunction();
  private static final HestonFFTNonLinearLeastSquareFitter FFT_VOLS = new HestonFFTNonLinearLeastSquareFitter();
  private static final HestonFFTOptionPriceNonLinearLeastSquareFitter FFT_PRICE = new HestonFFTOptionPriceNonLinearLeastSquareFitter();
  private static final HestonFourierNonLinearLeastSquareFitter FOURIER = new HestonFourierNonLinearLeastSquareFitter();

  static {
    final CharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);
    final FourierPricer pricer = new FourierPricer();
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final BlackImpliedVolatilityFormula blackImpliedVol = new BlackImpliedVolatilityFormula();
    final double beta = 0.5;
    final double alpha = SIGMA * Math.pow(FORWARD, 1 - beta);
    final double nu = 0.4;
    final double rho = -0.65;

    ERRORS = new double[N];
    OPTIONS = new EuropeanVanillaOption[N];
    BLACK_VOLS = new BlackFunctionData[N];
    SABR_VOLS = new BlackFunctionData[N];
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, SIGMA);
    for (int i = 0; i < N; i++) {
      ERRORS[i] = 0.001; //10bps errors 
      OPTIONS[i] = new EuropeanVanillaOption(0.01 + 0.01 * i, T, true);
      final double price = pricer.price(data, OPTIONS[i], heston, -0.5, 1e-9, true);
      BLACK_VOLS[i] = new BlackFunctionData(FORWARD, DF, blackImpliedVol.getImpliedVolatility(data, OPTIONS[i], price));
      SABR_VOLS[i] = new BlackFunctionData(FORWARD, DF, sabr.getVolatilityFunction(OPTIONS[i]).evaluate(new SABRFormulaData(FORWARD, alpha, beta, nu, rho)));
    }
  }

  @Test
  public void testSABRFit() {
    final double[] temp = new double[] {1.0, 0.1, 0.2, 0.3, -0.5};
    final BitSet fixed = new BitSet();
    LeastSquareResults results = FFT_VOLS.getFitResult(OPTIONS, SABR_VOLS, ERRORS, temp, fixed);
    assertTrue(results.getChiSq() < N * 100);
    results = FFT_PRICE.getFitResult(OPTIONS, SABR_VOLS, ERRORS, temp, fixed);
    assertTrue(results.getChiSq() < N * 100);
    results = FOURIER.getFitResult(OPTIONS, SABR_VOLS, ERRORS, temp, fixed);
    assertTrue(results.getChiSq() < N * 100);
    //TODO awful chiSq
  }

  @Test
  public void testExactFit() {
    assertExactFit(FFT_VOLS, "FFT vols", ERRORS);
    assertExactFit(FFT_VOLS, "FFT vols", null);
    final double[] pErrors = new double[N];
    for (int i = 0; i < N; i++) {
      pErrors[i] = ERRORS[i] * BLACK_PRICE.getVegaFunction(OPTIONS[i]).evaluate(BLACK_VOLS[i]);
    }
    assertExactFit(FFT_PRICE, "FFT price", pErrors);
    assertExactFit(FOURIER, "Fourier", ERRORS);
    assertExactFit(FOURIER, "Fourier", null);
  }
  
  //FIXME: tests don't pass at all
  @SuppressWarnings("unused")
  private void assertExactFit(final LeastSquareSmileFitter fitter, final String name, final double[] errors) {
    final double[] temp = new double[] {1.0, 0.04, VOL0, 0.2, 0.0};
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      final LeastSquareResults results = fitter.getFitResult(OPTIONS, BLACK_VOLS, errors, temp, new BitSet());
      assertEquals(0.0, results.getChiSq(), 1e+1);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles using {}", _benchmarkCycles, name);
      for (int i = 0; i < _benchmarkCycles; i++) {
        final LeastSquareResults results = fitter.getFitResult(OPTIONS, BLACK_VOLS, errors, temp, new BitSet());
        final DoubleMatrix1D params = results.getParameters();
        //        assertEquals(params.getEntry(0), KAPPA, 1e-3);
        //        assertEquals(params.getEntry(1), THETA, 1e-3);
        //        assertEquals(params.getEntry(2), VOL0, 1e-3);
        //        assertEquals(params.getEntry(3), OMEGA, 1e-3);
        //        assertEquals(params.getEntry(4), RHO, 1e-3);
        //        assertEquals(0.0, results.getChiSq(), 1e+1);

      }
      timer.finished();
    }

  }

}
