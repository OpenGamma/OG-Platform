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
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapForwardPurePDE;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplication;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapMonteCarloCalculator;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapPureMonteCarloCalculator;
import com.opengamma.analytics.financial.equity.variance.pricing.VolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;

/**
 * 
 */
public class VarianceSwapWithDividendsTest {

  private static final boolean PRINT = false; //set to false for push
  private static final boolean ASSERT = true; //set to true for push 
  private static final int N_SIMS = 10000; //put to 10,000 for push
  final static int seed = 123;
  private static final double MC_SD = 4.0;

  private static final VarianceSwapMonteCarloCalculator MC_CALCULATOR = new VarianceSwapMonteCarloCalculator(seed, true, true);
  private static final VarianceSwapPureMonteCarloCalculator MC_CALCULATOR_PURE = new VarianceSwapPureMonteCarloCalculator(seed, true, true);
  private static final EquityVarianceSwapStaticReplication STATIC_REPLICATION = new EquityVarianceSwapStaticReplication();
  private static final EquityVarianceSwapForwardPurePDE PDE_SOLVER = new EquityVarianceSwapForwardPurePDE();
  private static final double EXPIRY = 1.5;
  private static final double PURE_VOL = 0.5;
  private static final double VOL = 0.4;
  private static final double SPOT = 100.0;
  private static final double DRIFT = 0.1;

  @SuppressWarnings("unused")
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL_FLAT = new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(VOL));
  private static final PureLocalVolatilitySurface PURE_LOCAL_VOL_FLAT = new PureLocalVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
  private static final PureImpliedVolatilitySurface PURE_IMPLIED_VOL_FLAT = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = new YieldCurve(ConstantDoublesCurve.from(DRIFT));
  // private static final ForwardCurve FORWARD_CURVE;

  static {

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

    double[] tau = new double[0];
    double[] alpha = new double[0];
    double[] beta = new double[0];
    AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
  }

  @Test
  public void proportionalOnlyTest() {
    double[] tau = new double[] {EXPIRY - 0.5, EXPIRY + 0.1, EXPIRY + 0.6 };
    double[] alpha = new double[3];
    double[] beta = new double[] {0.1, 0.1, 0.1 };
    AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
  }

  @Test
  public void dividendsAfterExpiryTest() {
    double[] tau = new double[] {EXPIRY + 0.1, EXPIRY + 0.6 };
    double[] alpha = new double[] {0.02 * SPOT, 0.02 * SPOT };
    double[] beta = new double[] {0.02, 0.02 };
    AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
  }

  @Test
  public void dividendsBeforeExpiryTest() {
    double[] tau = new double[] {0.85, 1.2 };
    double[] alpha = new double[] {0.3 * SPOT, 0.2 * SPOT };
    double[] beta = new double[] {0.1, 0.2 };
    AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
  }

  @Test
  public void dividendsAtExpiryTest() {
    double[] tau = new double[] {1.2, EXPIRY };
    double[] alpha = new double[] {0.2 * SPOT, 0.1 * SPOT };
    double[] beta = new double[] {0.1, 0.2 };
    AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    testNumericsForFlatPureVol(dividends);
  }

  /**
   * Tests the Monte Carlo (which required a local volatility surface (strike)) against a static replication (which requires a pure implied volatility surface)
   * @param dividends
   */
  private void testNumericsForFlatPureVol(final AffineDividends dividends) {
    EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(SPOT, DISCOUNT_CURVE, dividends);
    LocalVolatilitySurfaceStrike localVol = VolatilitySurfaceConverter.convertLocalVolSurface(PURE_LOCAL_VOL_FLAT, divCurves);

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

    double fT = divCurves.getF(EXPIRY);

    //run Monte Carlo (simulating the actual stock process)
    double[] res = MC_CALCULATOR.solve(SPOT, dividends, EXPIRY, DISCOUNT_CURVE, localVol, N_SIMS);
    double mcST = res[0]; //The expected stock price at expiry
    double mcSDST = Math.sqrt(res[3]); //The Standard Deviation of the expected stock price at expiry
    double mcRV1 = res[1]; //The (annualised) expected variance correcting for dividends 
    double mcVarRV1 = res[4];// The variance of the expected variance correcting for dividends
    double mcRV2 = res[2]; //The (annualised) expected variance NOT correcting for dividends 
    double mcVarRV2 = res[5]; //The variance of expected variance NOT correcting for dividends 
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
    res = MC_CALCULATOR_PURE.solve(SPOT, dividends, EXPIRY, DISCOUNT_CURVE, PURE_LOCAL_VOL_FLAT, N_SIMS);
    double mcpST = res[0]; //see above for definition of these variables 
    double mcpSDST = Math.sqrt(res[3]);
    double mcpRV1 = res[1];
    double mcpVarRV1 = res[4];
    double mcpRV2 = res[2];
    double mcpVarRV2 = res[5];
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
    double[] tau = dividends.getTau();
    final double dt = 1. / 252;
    for (int i = 0; i < n; i++) {
      int steps = (int) (Math.ceil(tau[i] * 252));
      tau[i] = steps * dt;
    }

    //calculate by static replication using prices of PURE put and call prices  
    res = STATIC_REPLICATION.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_IMPLIED_VOL_FLAT);
    double srpRV1 = res[0] / EXPIRY;
    double srpRV2 = res[1] / EXPIRY;
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
    double srRV1 = res[0] / EXPIRY;
    double srRV2 = res[1] / EXPIRY;
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
    res = PDE_SOLVER.expectedVariance(SPOT, DISCOUNT_CURVE, dividends, EXPIRY, PURE_LOCAL_VOL_FLAT);
    double fpdeRV1 = res[0] / EXPIRY;
    double fpdeRV2 = res[1] / EXPIRY;
    if (PRINT) {
      System.out.format("Forward PDE:  RV1 = %1$.8f RV2 = %2$.8f%n", fpdeRV1, fpdeRV2);
    }
    if (ASSERT) {
      if (canPriceAnalytic) {
        assertEquals("Analytic V Forward PDE RV1", anVar1, fpdeRV1, 1e-7);
        assertEquals("Analytic V Forward PDE RV2", anVar2, fpdeRV2, 1e-7);
      } else {
        assertEquals("Static Rep (pure) V  Forward PDE RV1", srRV1, srpRV1, 2e-5);
        assertEquals("Static Rep (pure) V Forward PDE RV2", srRV2, srpRV2, 1e-5);
      }
    }

  }

}
