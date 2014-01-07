/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapBackwardsPurePDE;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapForwardPurePDE;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapMonteCarloCalculator;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplication;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapPureMonteCarloCalculator;
import com.opengamma.analytics.financial.equity.variance.pricing.VolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.MixedLogNormalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class VarianceSwapWithDividendsTest {

  private static final boolean PRINT = false; //set to false for push
  private static final boolean ASSERT = true; //set to true for push
  private static final int N_SIMS = 10000; //put to 10,000 for push
  final static int seed = 123;
  private static final double MC_SD = 4.0;

  private static final EquityVarianceSwapMonteCarloCalculator MC_CALCULATOR = new EquityVarianceSwapMonteCarloCalculator(seed, true, true);
  private static final VarianceSwapPureMonteCarloCalculator MC_CALCULATOR_PURE = new VarianceSwapPureMonteCarloCalculator(seed, true, true);
  private static final EquityVarianceSwapStaticReplication STATIC_REPLICATION = new EquityVarianceSwapStaticReplication();
  private static final EquityVarianceSwapForwardPurePDE PDE_FWD_SOLVER = new EquityVarianceSwapForwardPurePDE();
  private static final EquityVarianceSwapBackwardsPurePDE PDE_BKD_SOLVER = new EquityVarianceSwapBackwardsPurePDE();
  private static final double EXPIRY = 1.5;
  private static final double PURE_VOL = 0.5;
  private static final double SPOT = 123;
  private static final double DRIFT = 0.07;

  private static final PureLocalVolatilitySurface PURE_LOCAL_VOL_FLAT = new PureLocalVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
  private static final PureImpliedVolatilitySurface PURE_IMPLIED_VOL_FLAT = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
  private static final MultiHorizonMixedLogNormalModelData MLN_DATA;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = YieldCurve.from(ConstantDoublesCurve.from(DRIFT));

  static {
    final double[] weights = new double[] {0.8, 0.15, 0.05 };
    final double[] sigmas = new double[] {0.3, 0.5, 0.9 };
    final double[] mus = new double[] {0.04, 0.1, -0.2 };
    MLN_DATA = new MultiHorizonMixedLogNormalModelData(weights, sigmas, mus);
  }

  @SuppressWarnings("unused")
  @Test
  public void checkTest() {
    if (PRINT) {
      System.out.println("VarianceSwapWithDividendsTest PRINT must be set to false");
    }
    if (!ASSERT) {
      System.out.println("VarianceSwapWithDividendsTest ASSERT must be set to true, otherwise numbers are NOT tested");
    }
    if (N_SIMS > 10000) {
      System.out.println("VarianceSwapWithDividendsTest Too many simulations for Ant tests - change N_SIMS to 10000");
    }
  }

  @Test
  public void noDividendsTest() {
    final AffineDividends dividends = AffineDividends.noDividends();
    testNumericsForFlatPureVol(dividends);
    testNumerics(dividends, MLN_DATA, 1e-7);

  }

  @Test
  public void proportionalOnlyTest() {
    final double[] tau = new double[] {EXPIRY - 0.7, EXPIRY - 0.1, EXPIRY + 0.1 };
    final double[] alpha = new double[3];
    final double[] beta = new double[] {0.1, 0.1, 0.1 };
    final AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
    testNumerics(dividends, MLN_DATA, 1e-7);
  }

  @Test
  public void dividendsAfterExpiryTest() {
    final double[] tau = new double[] {EXPIRY + 0.1, EXPIRY + 0.6 };
    final double[] alpha = new double[] {0.1 * SPOT, 0.05 * SPOT };
    final double[] beta = new double[] {0.05, 0.1 };
    final AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
    testNumerics(dividends, MLN_DATA, 1e-7);
  }

  @Test
  public void dividendsBeforeExpiryTest() {
    final double[] tau = new double[] {0.85, 1.2 };
    final double[] alpha = new double[] {0.3 * SPOT, 0.2 * SPOT };
    final double[] beta = new double[] {0.1, 0.2 };
    final AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
    testNumerics(dividends, MLN_DATA, 1e-7);
  }

  @Test
  public void dividendsAtExpiryTest() {
    final double[] tau = new double[] {1.2, EXPIRY };
    final double[] alpha = new double[] {0.1 * SPOT, 0.05 * SPOT };
    final double[] beta = new double[] {0.1, 0.2 };
    final AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    //   testNumericsForFlatPureVol(dividends);
    testNumerics(dividends, MLN_DATA, 1e-7);
  }

  @Test
  public void testMixedLogNormalVolSurface() {

    final AffineDividends dividends = AffineDividends.noDividends();
    final ForwardCurve fwdCurve = new ForwardCurve(SPOT, DRIFT);
    final double sigma1 = 0.2;
    final double sigma2 = 1.0;
    final double w = 0.9;

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = fwdCurve.getForward(t);
        final boolean isCall = k > fwd;
        final double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };

    final BlackVolatilitySurfaceStrike surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));

    final double[] res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, surfaceStrike);
    final double rv = res[0] / EXPIRY;
    final double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;
    assertEquals(expected, rv, 2e-6); //TODO this should be better

    //  PDE_BKD_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, );
  }

  /**
   * Tests the Monte Carlo (which required a local volatility surface (strike)) against a static replication (which requires a pure implied volatility surface)
   * @param dividends
   */
  private void testNumericsForFlatPureVol(final AffineDividends dividends) {
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(SPOT, DISCOUNT_CURVE, dividends);
    final LocalVolatilitySurfaceStrike localVol = VolatilitySurfaceConverter.convertLocalVolSurface(PURE_LOCAL_VOL_FLAT, divCurves);

    //get the analytic values of the expected variance if there are no cash dividends
    boolean canPriceAnalytic = true;
    for (int i = 0; i < dividends.getNumberOfDividends(); i++) {
      if (dividends.getAlpha(i) > 0.0) {
        canPriceAnalytic = false;
        break;
      }
    }
    double anVar1 = 0;
    double anVar2 = 0;
    if (canPriceAnalytic) {
      int index = 0;
      double anCorr = 0;
      final int n = dividends.getNumberOfDividends();
      while (n > 0 && index < n && dividends.getTau(index) < EXPIRY) {
        anCorr += FunctionUtils.square(Math.log(1 - dividends.getBeta(index)));
        index++;
      }
      anVar1 = PURE_VOL * PURE_VOL;
      anVar2 = anVar1 + anCorr / EXPIRY;
      if (PRINT) {
        System.out.format("Analytic:  RV1 = %1$.8f RV2 = %2$.8f%n", anVar1, anVar2);
      }
    }

    final double fT = divCurves.getF(EXPIRY);

    //run Monte Carlo (simulating the actual stock process)
    double[] res = MC_CALCULATOR.solve(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, localVol, N_SIMS);
    final double mcST = res[0]; //The expected stock price at expiry
    final double mcSDST = Math.sqrt(res[3]); //The Standard Deviation of the expected stock price at expiry
    final double mcRV1 = res[1]; //The (annualised) expected variance correcting for dividends
    final double mcVarRV1 = res[4];// The variance of the expected variance correcting for dividends
    final double mcRV2 = res[2]; //The (annualised) expected variance NOT correcting for dividends
    final double mcVarRV2 = res[5]; //The variance of expected variance NOT correcting for dividends
    //      double mceK1 = (Math.sqrt(mcRV1) - mcVarRV1 / 8 / Math.pow(mcRV1, 1.5)); //very small bias correction applied here
    //      double sdK1 = Math.sqrt(mcVarRV1 / 4 / mcRV1 * (1 - mcVarRV1 / 16 / mcRV1 / mcRV1));
    //      double mceK2 = (Math.sqrt(mcRV2) - mcVarRV2 / 8 / Math.pow(mcRV2, 1.5)); //very small bias correction applied here
    //      double sdK2 = Math.sqrt(mcVarRV2 / 4 / mcRV2 * (1 - mcVarRV2 / 16 / mcRV2 / mcRV2));
    if (PRINT) {
      System.out.format("Monte Carlo: F_T = %1$.3f, s =  %2$.3f+-%3$.3f RV1 = %4$.8f+-%5$.8f RV2 = %6$.8f+-%7$.8f%n", fT, mcST, mcSDST, mcRV1, Math.sqrt(mcVarRV1), mcRV2, Math.sqrt(mcVarRV2));
    }
    assertEquals("E[S_T]", fT, mcST, MC_SD * mcSDST); //number of  standard deviations of MC error
    if (canPriceAnalytic && ASSERT) {
      assertEquals("Analytic V MC RV1", anVar1, mcRV1, MC_SD * Math.sqrt(mcVarRV1));
      assertEquals("Analytic V MC RV2", anVar2, mcRV2, MC_SD * Math.sqrt(mcVarRV2));
    }

    //run Monte Carlo (simulating the PURE stock process)
    res = MC_CALCULATOR_PURE.solve(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_LOCAL_VOL_FLAT, N_SIMS);
    final double mcpST = res[0]; //see above for definition of these variables
    final double mcpSDST = Math.sqrt(res[3]);
    final double mcpRV1 = res[1];
    final double mcpVarRV1 = res[4];
    final double mcpRV2 = res[2];
    final double mcpVarRV2 = res[5];
    if (PRINT) {
      System.out.format("Pure Monte Carlo: F_T = %1$.3f, s =  %2$.3f+-%3$.3f RV1 = %4$.8f+-%5$.8f RV2 = %6$.8f+-%7$.8f%n", fT, mcpST, mcpSDST, mcpRV1, Math.sqrt(mcpVarRV1), mcpRV2,
          Math.sqrt(mcpVarRV2));
    }
    if (ASSERT) {
      assertEquals("E[S_T]", fT, mcpST, MC_SD * mcpSDST); //number of  standard deviations of MC error
      if (canPriceAnalytic) {
        assertEquals("Analytic V MC (pure) RV1", anVar1, mcpRV1, MC_SD * Math.sqrt(mcpVarRV1));
        assertEquals("Analytic V MC (pure) RV2", anVar2, mcpRV2, MC_SD * Math.sqrt(mcpVarRV2));
      }
      //if the same seed is used, the two Monte Carlos should differ only by the discretisation error from using a Euler scheme (Euler is exact for a flat local volatility)
      assertEquals("MC (Pure) V MC  RV1", mcpRV1, mcRV1, 1e-4);
      assertEquals("MC (pure) V MC  RV2", mcpRV2, mcRV2, 1e-3); //TODO should have a better tolerance than this
    }

    //To match up with Monte Carlo, make all dividend times an integer number of days
    final int n = dividends.getNumberOfDividends();
    final double[] tau = dividends.getTau();
    final double dt = 1. / 252;
    for (int i = 0; i < n; i++) {
      final int steps = (int) (Math.ceil(tau[i] * 252));
      tau[i] = steps * dt;
    }

    //calculate by static replication using prices of PURE put and call prices
    res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_IMPLIED_VOL_FLAT);
    final double srpRV1 = res[0] / EXPIRY;
    final double srpRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Static replication (pure):  RV1 = %1$.8f RV2 = %2$.8f%n", srpRV1, srpRV2);
    }
    if (ASSERT) {
      if (canPriceAnalytic) {
        assertEquals("Analytic V Static Rep (pure) RV1", anVar1, srpRV1, 1e-8);
        assertEquals("Analytic V Static Rep (pure) RV2", anVar2, srpRV2, 1e-8);
      } else {
        assertEquals("Static Rep (pure) V MC RV1", srpRV1, mcpRV1, MC_SD * Math.sqrt(mcpVarRV1));
        assertEquals("Static Rep (pure) V MC RV2", srpRV2, mcpRV2, MC_SD * Math.sqrt(mcpVarRV2));
      }
    }

    //calculate by static replication using actual prices of put and call prices  (converted from pure prices)
    //NOTE, this surface is converted from the (smooth, flat) pure implied vol surface, and will have the correct discontinuities at cash dividend dates
    final BlackVolatilitySurfaceStrike impVol = VolatilitySurfaceConverter.convertImpliedVolSurface(PURE_IMPLIED_VOL_FLAT, divCurves);
    res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, impVol);
    final double srRV1 = res[0] / EXPIRY;
    final double srRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Static replication (real price):  RV1 = %1$.8f RV2 = %2$.8f%n", srRV1, srRV2);
    }
    if (ASSERT) {
      if (canPriceAnalytic) {
        assertEquals("Analytic V Static Rep (real price) RV1", anVar1, srRV1, 1e-8);
        assertEquals("Analytic V Static Rep (real price) RV2", anVar2, srRV2, 1e-8);
      } else {
        assertEquals("Static Rep (real price) V Static Rep (pure) RV1", srRV1, srpRV1, 2e-4); //TODO These should be closer
        assertEquals("Static Rep (real prcie) V Static Rep (pure) RV2", srRV2, srpRV2, 5e-5);
      }
    }

    //calculate by solving the forward PDE
    res = PDE_FWD_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_LOCAL_VOL_FLAT);
    final double fpdeRV1 = res[0] / EXPIRY;
    final double fpdeRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Forward PDE:  RV1 = %1$.8f RV2 = %2$.8f%n", fpdeRV1, fpdeRV2);
    }
    if (ASSERT) {
      if (canPriceAnalytic) {
        assertEquals("Analytic V Forward PDE RV1", anVar1, fpdeRV1, 2e-5);
        assertEquals("Analytic V Forward PDE RV2", anVar2, fpdeRV2, 2e-5);
      } else {
        assertEquals("Static Rep (pure) V  Forward PDE RV1", srpRV1, fpdeRV1, 2e-5);
        assertEquals("Static Rep (pure) V Forward PDE RV2", srpRV2, fpdeRV2, 5e-5);
      }
    }

    //calculate by solving the backwards PDE
    res = PDE_BKD_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_LOCAL_VOL_FLAT);
    final double bpdeRV1 = res[0] / EXPIRY;
    final double bpdeRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Backwards PDE:  RV1 = %1$.8f RV2 = %2$.8f%n", bpdeRV1, bpdeRV2);
    }
    if (ASSERT) {
      if (canPriceAnalytic) {
        assertEquals("Analytic V Forward PDE RV1", anVar1, bpdeRV1, 1e-7);
        assertEquals("Analytic V Forward PDE RV2", anVar2, bpdeRV2, 1e-7);
      } else {
        assertEquals("Static Rep (pure) V  Backwards PDE RV1", srpRV1, bpdeRV1, 1e-4);
        assertEquals("Static Rep (pure) V Backwards PDE RV2", srpRV2, bpdeRV2, 1e-4);
      }
    }
  }

  /**
   * Test the various numerical scheme when we have extraneously given pure (implied and local) volatility surfaces
   * @param dividends The dividend structure
   * @param pImpVol The pure implied volatility surface
   * @param plv The pure local volatility surface
   */
  private void testNumerics(final AffineDividends dividends, final MultiHorizonMixedLogNormalModelData data, final double defaultTol) {

    final ForwardCurve fc = new ForwardCurve(1.0);
    final LocalVolatilitySurfaceStrike lv = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    final PureLocalVolatilitySurface plv = new PureLocalVolatilitySurface(lv.getSurface());
    final BlackVolatilitySurfaceStrike iv = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    final PureImpliedVolatilitySurface piv = new PureImpliedVolatilitySurface(iv.getSurface());
    final double[] weights = data.getWeights();
    final double[] mus = data.getMus();
    final double[] sigmas = data.getVolatilities();
    final double m = weights.length;
    final int n = dividends.getNumberOfDividends();

    //get the analytic values of the expected variance if there are no cash dividends
    boolean canPriceAnalytic = true;
    for (int i = 0; i < n; i++) {
      if (dividends.getAlpha(i) > 0.0) {
        canPriceAnalytic = false;
        break;
      }
    }

    double expDivCorr = 0.0;
    double expDivNoCorr = 0.0;

    if (canPriceAnalytic) {
      double sum1 = 0.0;
      double sum2 = 0.0;
      for (int i = 0; i < m; i++) {
        sum1 += weights[i] * Math.exp(mus[i] * EXPIRY);
        sum2 += weights[i] * (mus[i] - sigmas[i] * sigmas[i] / 2);
      }
      expDivCorr = 2 * (Math.log(sum1) / EXPIRY - sum2);

      double anCorr = 0;
      int index = 0;
      while (n > 0 && index < n && dividends.getTau(index) < EXPIRY) {
        anCorr += FunctionUtils.square(Math.log(1 - dividends.getBeta(index)));
        index++;
      }
      expDivNoCorr = expDivCorr + anCorr / EXPIRY;
    }

    testNumerics(canPriceAnalytic, expDivCorr, expDivNoCorr, dividends, piv, plv, defaultTol);
  }

  /**
   * Test the various numerical scheme when we have extraneously given pure (implied and local) volatility surfaces
   * @param dividends The dividend structure
   * @param pImpVol The pure implied volatility surface
   * @param plv The pure local volatility surface
   */
  @SuppressWarnings("unused")
  private void testNumerics(final boolean isAnalytic, final double expDivCorr, final double expDivNoCorr, final AffineDividends dividends, final PureImpliedVolatilitySurface pImpVol,
      final PureLocalVolatilitySurface plv, final double defaultTol) {

    if (PRINT && isAnalytic) {
      System.out.format("Analytic:  RV1 = %1$.8f RV2 = %2$.8f%n", expDivCorr, expDivNoCorr);
    }

    //convert the pure (implied and local)
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(SPOT, DISCOUNT_CURVE, dividends);
    final LocalVolatilitySurfaceStrike localVol = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);
    final BlackVolatilitySurfaceStrike impVol = VolatilitySurfaceConverter.convertImpliedVolSurface(pImpVol, divCurves);
    final double fT = divCurves.getF(EXPIRY);

    //run Monte Carlo (simulating the PURE stock process)
    double[] res = MC_CALCULATOR_PURE.solve(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, plv, N_SIMS);
    final double mcpST = res[0]; //The expected stock price at expiry
    final double mcpSDST = Math.sqrt(res[3]); //The Standard Deviation of the expected stock price at expiry
    final double mcpRV1 = res[1]; //The (annualised) expected variance correcting for dividends
    final double mcpVarRV1 = res[4];// The variance of the expected variance correcting for dividends
    final double mcpRV2 = res[2]; //The (annualised) expected variance NOT correcting for dividends
    final double mcpVarRV2 = res[5]; //The variance of expected variance NOT correcting for dividends
    if (PRINT) {
      System.out.format("Pure Monte Carlo: F_T = %1$.3f, s =  %2$.3f+-%3$.3f RV1 = %4$.8f+-%5$.8f RV2 = %6$.8f+-%7$.8f%n", fT, mcpST, mcpSDST, mcpRV1, Math.sqrt(mcpVarRV1), mcpRV2,
          Math.sqrt(mcpVarRV2));
    }
    if (ASSERT) {
      assertEquals("E[S_T]", fT, mcpST, MC_SD * mcpSDST); //number of  standard deviations of MC error
      if (isAnalytic) {
        assertEquals("Analytic V MC RV1", expDivCorr, mcpRV1, MC_SD * Math.sqrt(mcpVarRV1));
        assertEquals("Analytic V MC RV2", expDivNoCorr, mcpRV2, MC_SD * Math.sqrt(mcpVarRV2));
      }
    }

    //run the Monte Carlo (simulating the ACTUAL stock process)
    res = MC_CALCULATOR.solve(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, localVol, N_SIMS);
    final double mcST = res[0]; //see above for definition of these variables
    final double mcSDST = Math.sqrt(res[3]);
    final double mcRV1 = res[1];
    final double mcVarRV1 = res[4];
    final double mcRV2 = res[2];
    final double mcVarRV2 = res[5];
    if (PRINT) {
      System.out.format("Monte Carlo: F_T = %1$.3f, s =  %2$.3f+-%3$.3f RV1 = %4$.8f+-%5$.8f RV2 = %6$.8f+-%7$.8f%n", fT, mcST, mcSDST, mcRV1, Math.sqrt(mcVarRV1), mcRV2,
          Math.sqrt(mcVarRV2));
    }
    if (ASSERT) {
      assertEquals("E[S_T]", fT, mcST, MC_SD * mcSDST); //number of  standard deviations of MC error
      if (isAnalytic) {
        assertEquals("Analytic V MC RV1", expDivCorr, mcRV1, MC_SD * Math.sqrt(mcVarRV1));
        assertEquals("Analytic V MC RV2", expDivNoCorr, mcRV2, MC_SD * Math.sqrt(mcVarRV2));
      }
    }

    //calculate by static replication using prices of PURE put and call prices (from the pure implied volatility surface)
    res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, pImpVol);
    final double srpRV1 = res[0] / EXPIRY;
    final double srpRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Static replication (pure):  RV1 = %1$.8f RV2 = %2$.8f%n", srpRV1, srpRV2);
    }
    if (ASSERT) {
      if (isAnalytic) {
        assertEquals("Analytic V Static Rep (pure) RV1", expDivCorr, srpRV1, defaultTol);
        assertEquals("Analytic V Static Rep (pure) RV2", expDivNoCorr, srpRV2, defaultTol);
      } else {
        assertEquals("MC V Static Rep (pure) RV1", mcpRV1, srpRV1, MC_SD * Math.sqrt(mcpVarRV1));
        assertEquals("MC V Static Rep (pure) RV2", mcpRV2, srpRV2, MC_SD * Math.sqrt(mcpVarRV2));
      }
    }

    //calculate by static replication using actual prices of put and call prices  (from the (converted) implied volatility surface)
    res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, impVol);
    final double srRV1 = res[0] / EXPIRY;
    final double srRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Static replication (real price):  RV1 = %1$.8f RV2 = %2$.8f%n", srRV1, srRV2);
    }
    if (ASSERT) {
      if (isAnalytic) {
        assertEquals("Analytic V Static Rep (actual) RV1", expDivCorr, srRV1, defaultTol);
        assertEquals("Analytic V Static Rep (actual) RV2", expDivNoCorr, srRV2, defaultTol);
      } else {
        assertEquals("MC V Static Rep (actual) RV1", mcRV1, srRV1, MC_SD * Math.sqrt(mcVarRV1));
        assertEquals("MC V Static Rep (actual) RV2", mcRV2, srRV2, MC_SD * Math.sqrt(mcVarRV2));
      }
    }

    //calculate by solving the forward PDE using the pure local volatility to get pure option prices, before pricing the variance swaps via static replication
    res = PDE_FWD_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, plv);
    final double fpdeRV1 = res[0] / EXPIRY;
    final double fpdeRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Forward PDE:  RV1 = %1$.8f RV2 = %2$.8f%n", fpdeRV1, fpdeRV2);
    }
    if (ASSERT) {
      if (isAnalytic) {
        assertEquals("Analytic V Forward PDE RV1", expDivCorr, fpdeRV1, 1e4 * defaultTol); //poor tolerance
        assertEquals("Analytic V Forward PDE RV2", expDivNoCorr, fpdeRV2, 1e4 * defaultTol);
      } else {
        assertEquals("Static Rep (pure) V  Forward PDE RV1", srpRV1, fpdeRV1, 1e4 * defaultTol);
        assertEquals("Static Rep (pure) V Forward PDE RV2", srpRV2, fpdeRV2, 1e4 * defaultTol);
      }
    }

    //calculate by solving the backwards PDE using the pure local volatility surface
    res = PDE_BKD_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, plv);
    final double bpdeRV1 = res[0] / EXPIRY;
    final double bpdeRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Backwards PDE:  RV1 = %1$.8f RV2 = %2$.8f%n", bpdeRV1, bpdeRV2);
    }
    if (ASSERT) {
      if (isAnalytic) {
        assertEquals("Analytic V backwards PDE RV1", expDivCorr, bpdeRV1, 1e4 * defaultTol);
        assertEquals("Analytic V backwards PDE RV2", expDivNoCorr, bpdeRV2, 1e4 * defaultTol);
      } else {
        assertEquals("Static Rep (pure) V  Backwards (pure) PDE RV1", srpRV1, bpdeRV1, 1e5 * defaultTol);
        assertEquals("Static Rep (pure) V Backwards (pure) PDE RV2", srpRV2, bpdeRV2, 1e5 * defaultTol);
      }
    }

  }

}
