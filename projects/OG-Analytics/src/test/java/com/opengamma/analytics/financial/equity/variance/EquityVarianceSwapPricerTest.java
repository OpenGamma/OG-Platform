/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapPricer;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityVarianceSwapPricerTest {

  private static final boolean PRINT = false;

  private static final double SPOT = 65.4;
  private static final String[] EXPIRY_LABELS = new String[] {"1W", "2W", "1M", "3M", "6M", "1Y", "2Y"};
  private static final double[] EXPIRIES = new double[] {1. / 52, 2. / 52, 1. / 12, 3. / 12, 6. / 12, 1.0, 2.0};
  private static final double[][] STRIKES = new double[][] { {50, 55, 60, 65, 70, 75, 80}, {50, 55, 60, 65, 70, 75, 80}, {50, 55, 60, 65, 70, 75, 80},
    {40, 50, 60, 70, 80, 90}, {40, 50, 60, 70, 80, 90, 100}, {30, 50, 60, 70, 80, 90, 100}, {20, 40, 55, 65, 75, 90, 105, 125}};
  //  private static final double[][] OTM_PRICES_FLAT;
  //  private static final double[][] OTM_PRICES;
  //  private static final SmileSurfaceDataBundle MARKET_VOLS_FLAT_NODIVS;
  //  private static final SmileSurfaceDataBundle MARKET_VOLS_FLAT
  //  private static final SmileSurfaceDataBundle MARKET_VOLS;
  private static final double PURE_VOL = 0.45;
  private static final PureImpliedVolatilitySurface PURE_VOL_SURFACE;
  private static final double[] TAU = new double[] {5. / 12, 11. / 12, 17. / 12, 23. / 12, 29. / 12};
  private static final double[] ALPHA = new double[] {3.0, 2.0, 1.0, 0.0, 0.0};
  private static final double[] BETA = new double[] {0.0, 0.02, 0.03, 0.04, 0.05};
  private static final AffineDividends NULL_DIVIDENDS = AffineDividends.noDividends();
  //private static final AffineDividends ZERO_DIVIDENDS = new AffineDividends(TAU, new double[5], new double[5]);
  private static final AffineDividends DIVIDENDS = new AffineDividends(TAU, ALPHA, BETA);

  private static final double R = 0.07;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = new YieldCurve("Discount", ConstantDoublesCurve.from(R));
  private static final VarianceSwap VS = new VarianceSwap(0, 0.75, 0.75, 0.0, 1.0, Currency.EUR, 252, 252, 0, new double[0], new double[0]);
  private static final EquityVarianceSwap EVS_COR_FRO_DIVS = new EquityVarianceSwap(VS, true);
  private static final EquityVarianceSwap EVS = new EquityVarianceSwap(VS, false);
  private static final EquityVarianceSwapPricer PRICER = EquityVarianceSwapPricer.builder().create();

  static {
    //    PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
    //find "market" prices if the pure implied volatility surface was flat
    //   OTM_PRICES_FLAT = getOptionPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);

    final double[] weights = new double[] {0.15, 0.8, 0.05};
    final double[] sigma = new double[] {0.15, 0.3, 0.8};
    final double[] mu = new double[] {0.04, 0.02, -0.2};

    final MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(weights, sigma, mu);
    final BlackVolatilitySurfaceStrike temp = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(new ForwardCurve(1.0), data);
    PURE_VOL_SURFACE = new PureImpliedVolatilitySurface(temp.getSurface());

    //get the 'market' prices for the hypothetical pure implied volatility surface
    //    OTM_PRICES = getOptionPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
    //    MARKET_VOLS = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
    //    MARKET_VOLS_FLAT = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);

  }

  @Test
  public void printOnTest() {
    if (PRINT) {
      System.out.println("EquityVarianceSwapPricerTest: true PRINT to false");
    }
  }

  @Test
  public void printPrices() {
    if (PRINT) {
      final PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
      printPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
      printPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, NULL_DIVIDENDS, PURE_VOL_SURFACE);
      printPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
    }
  }

  private void printPrices(final double spot, final double[] expiries, final double[][] strikes, final YieldAndDiscountCurve discountCurve,
      final AffineDividends dividends, final PureImpliedVolatilitySurface surf) {

    final SmileSurfaceDataBundle v1 = getMarketVols(spot, expiries, strikes, discountCurve, dividends, surf);

    final int n = expiries.length;
    System.out.print("\n");
    int mMax = 0;
    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      if (m > mMax) {
        mMax = m;
      }
    }

    System.out.print("\\begin{tabular}{|c|");
    for (int i = 0; i < mMax; i++) {
      System.out.print("c|");
    }
    System.out.print("}\n\\hline\n Expiry\\textbackslash index");
    for (int i = 0; i < mMax; i++) {
      System.out.print("&" + (i + 1));
    }
    System.out.print("\\\\\n\\hline\n");

    for (int i = 0; i < n; i++) {
      System.out.print(EXPIRY_LABELS[i] + "&");
      final int m = strikes[i].length;
      for (int j = 0; j < m; j++) {
        final double vol = v1.getVolatilities()[i][j];
        if (j < mMax - 1) {
          System.out.printf("%1.3f & ", vol);
        } else {
          System.out.printf("%1.3f", vol);
        }
      }
      for (int j = m; j < mMax - 1; j++) {
        System.out.print("&");
      }
      System.out.printf("\\\\");

      System.out.print("\n\\hline\n");
    }
    System.out.print("\\end{tabular}\n");
  }

  //  /**
  //   * This is really just testing whether we can recover the flat pure implied volatility surface from the option prices - the code paths after that point are identical
  //   */
  //  @Test
  //  public void flatSurfaceTest() {
  //    EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
  //    EquityVarianceSwapStaticReplication staRep = new EquityVarianceSwapStaticReplication();
  //    double[] rvExpected = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, DIVIDENDS, EVS.getTimeToObsEnd(), new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL)));
  //    @SuppressWarnings("unused")
  //    double[] noDivRVExpected = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EVS.getTimeToObsEnd(), new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL)));
  //
  //    double rv1 = pricer.priceFromOTMPrices(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);
  //    double rv2 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);
  //    double rv1PDE = pricer.priceFromLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS_FLAT);
  //    double rv2PDE = pricer.priceFromLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS_FLAT);
  //    @SuppressWarnings("unused")
  //    double noDivRV = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);
  //
  //    if (PRINT) {
  //      System.out.println(rvExpected[0] + "\t" + rvExpected[1]);
  //      System.out.println(rv1 + "\t" + rv2);
  //      System.out.println(rv1PDE + "\t" + rv2PDE);
  //    }
  //
  //    assertEquals("div corrected", rvExpected[0], rv1, 1e-10);
  //    assertEquals("div uncorrected", rvExpected[1], rv2, 1e-10);
  //    //we loose a lot of accuracy going via the forward PDE, but it is hardly terrible
  //    //TODO investigate the source of this - it is most likely because of crude integration that is done, rather than the PDE solver per se.
  //    assertEquals("div corrected PDE", rvExpected[0], rv1PDE, 1e-5);
  //    assertEquals("div uncorrected PDE", rvExpected[1], rv2PDE, 1e-5);
  //
  //    //in this case the reconstructed implied volatility surface (which is identical to the pure one as no dividends are assumed) is NOT flat so we would not expect the items
  //    //below to be equal
  //    //assertEquals("no divs", noDivRVExpected[0], noDivRV, 1e-10);
  //  }
  //
  //  @Test
  //  //  (enabled = false)
  //  public void nonFlatSurfaceTest() {
  //
  //    EquityVarianceSwapStaticReplication staRep = new EquityVarianceSwapStaticReplication();
  //    double[] rv = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, DIVIDENDS, EVS.getTimeToObsEnd(), PURE_VOL_SURFACE);
  //
  //    EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
  //    double rv1 = pricer.priceFromOTMPrices(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
  //    double rv2 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
  //    double rv3 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
  //
  //    double rv1PDE = pricer.priceFromLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
  //    double rv2PDE = pricer.priceFromLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS,
  //
  //    if (PRINT) {
  //      System.out.println(rv[0] + "\t" + rv[1]);
  //      System.out.println(rv1 + "\t" + rv2 + "\t" + rv3);
  //      System.out.println(rv1PDE + "\t" + rv2PDE);
  //    }
  //
  //    /*
  //     * Starting from a finite number of market prices (albeit synthetically generated in this case), once cannot reproduce the entire (hypothetical) implied volatility
  //     * surface, especially at extreme strikes where there is no market information. Since expected variance depends on the entire smile (theoretically from zero to infinite strike)
  //     * one cannot hope to make too accurate estimate from the prices of liquid European options.
  //     */
  //    assertEquals("div corrected", rv[0], rv1, 5e-3);
  //    assertEquals("div uncorrected", rv[1], rv2, 5e-3);
  //
  //    // However these should agree better, as they are just two different numerical schemes using the same date
  //    assertEquals("div corrected PDE", rv1, rv1PDE, 5e-4);
  //    assertEquals("div uncorrected PDE", rv1, rv1PDE, 5e-4);
  //  }

  @Test
  public void deltaWithStickyLocalVolTest() {
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
      final double d1 = PRICER.deltaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.deltaWithStickyLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.deltaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.deltaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.deltaWithStickyLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.deltaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  @Test
  public void gammaWithStickyLocalVolTest() {
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
      final EquityVarianceSwapPricer pricer = EquityVarianceSwapPricer.builder().create();
      final double d1 = pricer.gammaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = pricer.gammaWithStickyLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = pricer.gammaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = pricer.gammaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = pricer.gammaWithStickyLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = pricer.gammaWithStickyLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs:\t" + d1);
      System.out.println("Flat, divs corr:\t" + d2);
      System.out.println("Flat, divs not corr:\t" + d3);
      System.out.println("Non-flat, no divs:\t" + d4);
      System.out.println("Non-flat, divs corr:\t" + d5);
      System.out.println("Non-flat, divs not corr:\t" + d6);
    }
  }

  @Test
  public void vegaLocalVolTest() {
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
      final double d1 = PRICER.vegaLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.vegaLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.vegaLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.vegaLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.vegaLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.vegaLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  @Test
  public void vegaPureLocalVolTest() {
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
      final double d1 = PRICER.vegaPureLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v1);
      final double d2 = PRICER.vegaPureLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v2);
      final double d3 = PRICER.vegaPureLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v3);
      final double d4 = PRICER.vegaPureLocalVol(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, v4);
      final double d5 = PRICER.vegaPureLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v5);
      final double d6 = PRICER.vegaPureLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, v6);

      System.out.println("Flat, no divs: " + d1);
      System.out.println("Flat, divs corr: " + d2);
      System.out.println("Flat, divs not corr: " + d3);
      System.out.println("Non-flat, no divs: " + d4);
      System.out.println("Non-flat, divs corr: " + d5);
      System.out.println("Non-flat, divs not corr: " + d6);
    }
  }

  //  /**
  //   * Compute actual option (i.e. as would be observed in the market) from a hypothetical <b>pure</b> implied volatility surface and dividend assumptions
  //   * @param spot The current level of the stock or index
  //   * @param expiries expiries of option strips
  //   * @param strikes strikes at each expiry
  //   * @param discountCurve The discount curve
  //   * @param dividends dividend assumptions
  //   * @param surf hypothetical <b>pure</b> implied volatility surface
  //   * @return Market observed option prices
  //   */
  //  private static double[][] getOptionPrices(final double spot, final double[] expiries, final double[][] strikes, final YieldAndDiscountCurve discountCurve,
  //      final AffineDividends dividends, final PureImpliedVolatilitySurface surf) {
  //    final int nExp = expiries.length;
  //    final double[][] prices = new double[nExp][];
  //    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
  //    for (int i = 0; i < nExp; i++) {
  //      final int n = strikes[i].length;
  //      prices[i] = new double[n];
  //      final double t = expiries[i];
  //      final double f = divCurves.getF(t);
  //      final double d = divCurves.getD(t);
  //      final double p = discountCurve.getDiscountFactor(t);
  //      for (int j = 0; j < n; j++) {
  //        final double x = (strikes[i][j] - d) / (f - d);
  //        final boolean isCall = x >= 1.0;
  //        final double vol = surf.getVolatility(t, x);
  //        prices[i][j] = p * (f - d) * BlackFormulaRepository.price(1.0, x, t, vol, isCall);
  //      }
  //    }
  //    return prices;
  //  }

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
