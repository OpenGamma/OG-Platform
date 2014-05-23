/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplicationPricer;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.MixedLogNormalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityVarianceSwapStaticReplicationPricerTest {

  private static final boolean PRINT = false;

  private static final double SPOT = 65.4;
  private static final String[] EXPIRY_LABELS = new String[] {"1W", "2W", "1M", "3M", "6M", "1Y", "2Y" };
  private static final double[] EXPIRIES = new double[] {1. / 52, 2. / 52, 1. / 12, 3. / 12, 6. / 12, 1.0, 2.0 };
  private static final double[][] STRIKES = new double[][] { {50, 55, 60, 65, 70, 75, 80 }, {50, 55, 60, 65, 70, 75, 80 }, {50, 55, 60, 65, 70, 75, 80 },
      {40, 50, 60, 70, 80, 90 }, {40, 50, 60, 70, 80, 90, 100 }, {30, 50, 60, 70, 80, 90, 100 }, {20, 40, 55, 65, 75, 90, 105, 125 } };
  private static final double PURE_VOL = 0.45;
  private static final PureImpliedVolatilitySurface PURE_VOL_SURFACE;
  private static final double[] TAU = new double[] {5. / 12, 11. / 12, 17. / 12, 23. / 12, 29. / 12 };
  private static final double[] ALPHA = new double[] {3.0, 2.0, 1.0, 0.0, 0.0 };
  private static final double[] BETA = new double[] {0.0, 0.02, 0.03, 0.04, 0.05 };
  private static final AffineDividends NULL_DIVIDENDS = AffineDividends.noDividends();
  private static final AffineDividends DIVIDENDS = new AffineDividends(TAU, ALPHA, BETA);

  private static final double R = 0.07;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = new YieldCurve("Discount", ConstantDoublesCurve.from(R));
  private static final VarianceSwap VS = new VarianceSwap(0, 0.75, 0.75, 0.0, 1.0, Currency.USD, 252, 252, 0, new double[0], new double[0]);
  private static final EquityVarianceSwap EVS_COR_FRO_DIVS = new EquityVarianceSwap(VS, true);
  private static final EquityVarianceSwap EVS = new EquityVarianceSwap(VS, false);
  private static final EquityVarianceSwapStaticReplicationPricer PRICER = EquityVarianceSwapStaticReplicationPricer.builder().create();

  static {

    final double[] weights = new double[] {0.15, 0.8, 0.05 };
    final double[] sigma = new double[] {0.15, 0.3, 0.8 };
    final double[] mu = new double[] {0.04, 0.02, -0.2 };

    final MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(weights, sigma, mu);
    final BlackVolatilitySurfaceStrike temp = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(new ForwardCurve(1.0), data);
    PURE_VOL_SURFACE = new PureImpliedVolatilitySurface(temp.getSurface());

  }

  @Test
  public void bucketedVegaTest() {
    if (PRINT) {
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      final double[][] res1 = PRICER.bucketedVega(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v1);
      final double[][] res2 = PRICER.bucketedVega(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double[][] res3 = PRICER.bucketedVega(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double[][] res4 = PRICER.bucketedVega(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double[][] res5 = PRICER.bucketedVega(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double[][] res6 = PRICER.bucketedVega(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      printBucketedVega("Flat surface - no dividends", res1);
      printBucketedVega("Flat surface - dividends corrected", res2);
      printBucketedVega("Flat surface - dividends not corrected", res3);
      printBucketedVega("Non-flat surface - no dividends", res4);
      printBucketedVega("Non-flat surface - dividends corrected", res5);
      printBucketedVega("Non-flat surface - dividends not corrected", res6);
    }
  }

  private void printBucketedVega(final String label, final double[][] data) {
    final int n = data.length;
    ArgumentChecker.isTrue(n == EXPIRY_LABELS.length, "data wrong length");
    System.out.println(label);
    for (int i = 0; i < n; i++) {
      System.out.print(EXPIRY_LABELS[i] + "\t");
      final int m = data[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(data[i][j] + "\t");
      }
      System.out.print("\n");
    }
  }

  @Test
  public void dividendSensitivityWithStickyImpVolTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));

      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      //    double[][] d1 = pricer.dividendSensitivityWithStickyImpliedVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, ZERO_DIVIDENDS, v1);
      final double[][] d2 = PRICER.dividendSensitivityWithStickyImpliedVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double[][] d3 = PRICER.dividendSensitivityWithStickyImpliedVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      //    double[][] d4 = pricer.dividendSensitivityWithStickyImpliedVol(EVS, SPOT, DISCOUNT_CURVE, ZERO_DIVIDENDS, v4);
      final double[][] d5 = PRICER.dividendSensitivityWithStickyImpliedVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double[][] d6 = PRICER.dividendSensitivityWithStickyImpliedVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      //printDividendSense("Flat surface - no dividends:", d1);
      printDividendSense("Flat, divs corr:", d2);
      printDividendSense("Flat, divs not corr:", d3);
      //  printDividendSense(" Non-flat surface - no dividends:", d4);
      printDividendSense("Non-flat, divs corr:", d5);
      printDividendSense("Non-flat, divs not corr:", d6);
    }
  }

  private void printDividendSense(final String label, final double[][] data) {
    System.out.print(label + "\t");
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      final int m = data[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(data[i][j] + "\t");
      }
    }
    System.out.print("\n");

  }

  @Test
  public void deltaWithDtickyStikeTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.deltaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.deltaWithStickyStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.deltaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.deltaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.deltaWithStickyStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.deltaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  @Test
  public void deltaWithStickyPureStrikeTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.deltaWithStickyPureStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.deltaWithStickyPureStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  @Test
  public void gammaStickyStikeTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.gammaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.gammaWithStickyStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.gammaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.gammaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.gammaWithStickyStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.gammaWithStickyStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs:\t" + d1);
      System.out.println("Flat, divs corr:\t" + d2);
      System.out.println("Flat, divs not corr:\t" + d3);
      System.out.println("Non-flat, no divs:\t" + d4);
      System.out.println("Non-flat, divs corr:\t" + d5);
      System.out.println("Non-flat, divs not corr:\t" + d6);
    }
  }

  @Test
  public void gammaWithStickyPureStrikeTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.deltaWithStickyPureStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.deltaWithStickyPureStrike(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.deltaWithStickyPureStrike(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs:\t" + d1);
      System.out.println("Flat, divs corr:\t" + d2);
      System.out.println("Flat, divs not corr:\t" + d3);
      System.out.println("Non-flat, no divs:\t" + d4);
      System.out.println("Non-flat, divs corr:\t" + d5);
      System.out.println("Non-flat, divs not corr:\t" + d6);
    }
  }

  @Test
  public void vegaImpVolTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.vegaImpVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.vegaImpVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.vegaImpVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.vegaImpVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.vegaImpVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.vegaImpVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  @Test
  public void vegaPureImpVolTest() {
    if (PRINT) {
      //make all the implied volatility scenarios with the spot fixed
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      final SmileSurfaceDataBundle v1 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, flat);
      final SmileSurfaceDataBundle v2 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      final SmileSurfaceDataBundle v3 = v2;
      final SmileSurfaceDataBundle v4 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v5 = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
      final SmileSurfaceDataBundle v6 = v5;

      //now assume that the implied volatilities remain fixed as the spot moves
      final double d1 = PRICER.vegaPureImpVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.vegaPureImpVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.vegaPureImpVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.vegaPureImpVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.vegaPureImpVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.vegaPureImpVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  private static SmileSurfaceDataBundle getMarketVols(final double spot, final double[] expiries, final double[][] strikes, final YieldAndDiscountCurve discountCurve,
      final AffineDividends dividends, final PureImpliedVolatilitySurface surf) {
    final int nExp = expiries.length;
    final double[] f = new double[nExp];
    final double[][] vols = new double[nExp][];
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    for (int i = 0; i < nExp; i++) {
      final int n = strikes[i].length;
      vols[i] = new double[n];
      final double t = expiries[i];
      f[i] = divCurves.getF(t);
      final double d = divCurves.getD(t);
      for (int j = 0; j < n; j++) {
        final double x = (strikes[i][j] - d) / (f[i] - d);
        final boolean isCall = x >= 1.0;
        final double pVol = surf.getVolatility(t, x);
        final double p = (f[i] - d) * BlackFormulaRepository.price(1.0, x, t, pVol, isCall);
        vols[i][j] = BlackFormulaRepository.impliedVolatility(p, f[i], strikes[i][j], t, pVol);
      }
    }
    return new StandardSmileSurfaceDataBundle(spot, f, expiries, strikes, vols, Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE);
  }
}
