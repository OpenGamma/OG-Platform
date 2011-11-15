/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.FourierPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.HestonVolatilityFunction;
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
  private static final double VOL0 = 1.0 * THETA; // start level
  private static final double OMEGA = 0.25; // vol-of-vol
  private static final double RHO = -0.7; // correlation
  private static final boolean FIX_VOL0 = true;

  private static final int N = 7;

  private static final EuropeanVanillaOption[] OPTIONS;
  private static final BlackFunctionData[] BLACK_VOLS;
  private static final BlackFunctionData[] SABR_VOLS;
  private static final BlackPriceFunction BLACK_PRICE = new BlackPriceFunction();
  private static final HestonFFTSmileFitter FFT_VOL_FITTER = new HestonFFTSmileFitter(FIX_VOL0);
  private static final HestonFFTPriceFitter FFT_PRICE_FITTER = new HestonFFTPriceFitter(FIX_VOL0);
  private static final HestonFourierSmileFitter FOURIER_VOL_FITTER = new HestonFourierSmileFitter(FIX_VOL0);

  static {
    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);
    final FourierPricer pricer = new FourierPricer();
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final BlackImpliedVolatilityFormula blackImpliedVol = new BlackImpliedVolatilityFormula();
    final double beta = 0.5;
    final double alpha = SIGMA * Math.pow(FORWARD, 1 - beta);
    final double nu = 0.4;
    final double rho = -0.3;

    OPTIONS = new EuropeanVanillaOption[N];
    BLACK_VOLS = new BlackFunctionData[N];
    SABR_VOLS = new BlackFunctionData[N];
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, SIGMA);
    for (int i = 0; i < N; i++) {

      OPTIONS[i] = new EuropeanVanillaOption(0.01 + 0.01 * i, T, true);
      //using Fourier integral here rather than FFT 
      final double price = pricer.price(data, OPTIONS[i], heston, -0.5, 1e-10, true);
      BLACK_VOLS[i] = new BlackFunctionData(FORWARD, DF, blackImpliedVol.getImpliedVolatility(data, OPTIONS[i], price));
      SABR_VOLS[i] = new BlackFunctionData(FORWARD, DF, sabr.getVolatilityFunction(OPTIONS[i], FORWARD).evaluate(new SABRFormulaData(alpha, beta, rho, nu)));
    }
  }

  @Test
  public void testSABRFit() {

    double[] errors = new double[N];
    for (int i = 0; i < N; i++) {
      errors[i] = 0.01; //1pc errors 
    }

    final double[] temp = new double[] {0.2, 0.1, 0.3, -0.7 };
    final BitSet fixed = new BitSet();
    LeastSquareResults results = FFT_VOL_FITTER.getFitResult(OPTIONS, SABR_VOLS, errors, temp, fixed);
    //  System.out.println(" chi^2: " + results.getChiSq() + "\n" + results.getParameters());
    assertTrue(results.getChiSq() < N);
    results = FFT_PRICE_FITTER.getFitResult(OPTIONS, SABR_VOLS, errors, temp, fixed);
    //   System.out.println(" chi^2: " + results.getChiSq() + "\n" + results.getParameters());
    assertTrue(results.getChiSq() < N);
    results = FOURIER_VOL_FITTER.getFitResult(OPTIONS, SABR_VOLS, errors, temp, fixed);
    // System.out.println(" chi^2: " + results.getChiSq() + "\n" + results.getParameters());
    assertTrue(results.getChiSq() < N);
    //TODO awful chiSq
  }

  @Test
  public void testExactFit() {

    double[] errors = new double[N];
    Arrays.fill(errors, 1e-4);//1bps errors 

    assertExactFit(FFT_VOL_FITTER, "FFT vols", errors, true);
    final double[] pErrors = new double[N];
    //where doing a least square fit by price, having errors be the invease of vega makes it similar to least square by vols 
    for (int i = 0; i < N; i++) {
      pErrors[i] = 2e-6 * FORWARD / BLACK_PRICE.getVegaFunction(OPTIONS[i]).evaluate(BLACK_VOLS[i]);
    }
    assertExactFit(FFT_PRICE_FITTER, "FFT price", pErrors, false); //does not recover starting vols 
    assertExactFit(FOURIER_VOL_FITTER, "Fourier", errors, true);

  }

  @Test(enabled = false)
  public void testExactFitNewMethod() {
    double[] errors = new double[N];
    Arrays.fill(errors, 1e-4);//1bps errors 
    final double[] strikes = new double[N];
    final double[] vols = new double[N];
    for (int i = 0; i < N; i++) {
      strikes[i] = OPTIONS[i].getStrike();
      vols[i] = BLACK_VOLS[i].getBlackVolatility();
    }
    HestonModelFitter fitter = new HestonModelFitter(FORWARD, strikes, T, vols, errors, new HestonVolatilityFunction());
    final double[] initial = new double[] {2.0, 0.05, VOL0, 0.2, -0.4 };
    final BitSet fixed = new BitSet();
    fixed.set(2);
    LeastSquareResults res = fitter.solve(new DoubleMatrix1D(initial), fixed);
    System.out.println(res.getChiSq());
  }

  private void assertExactFit(final LeastSquareSmileFitter fitter, final String name, final double[] errors, boolean testParms) {
    final double[] temp = new double[] {2.0, 0.05, 0.2, -0.4 };
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      final LeastSquareResults results = fitter.getFitResult(OPTIONS, BLACK_VOLS, errors, temp, new BitSet());
      assertEquals(0.0, results.getChiSq(), 1e+1);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles using {}", _benchmarkCycles, name);
      for (int i = 0; i < _benchmarkCycles; i++) {
        final LeastSquareResults results = fitter.getFitResult(OPTIONS, BLACK_VOLS, errors, temp, new BitSet());
        final DoubleMatrix1D params = results.getParameters();

        //System.out.println(name + " chi^2: " + results.getChiSq() + "\n" + params);

        assertEquals(0.0, results.getChiSq(), 1e-1);
        if (testParms) {
          assertEquals(KAPPA, params.getEntry(0), 1e-1); //kappa hard to pin down
          assertEquals(THETA, params.getEntry(1), 1e-3);
          assertEquals(OMEGA, params.getEntry(2), 5e-3);
          assertEquals(RHO, params.getEntry(3), 5e-3);
        }

      }
      timer.finished();
    }

  }

}
