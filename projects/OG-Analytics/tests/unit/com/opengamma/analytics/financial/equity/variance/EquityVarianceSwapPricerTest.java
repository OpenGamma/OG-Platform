/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.derivative.EquityVarianceSwap;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapPricer;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class EquityVarianceSwapPricerTest {

  private static final boolean PRINT = false;

  private static final double SPOT = 65.4;
  private static final double[] EXPIRIES = new double[] {1. / 52, 2. / 52, 1. / 12, 3. / 12, 6. / 12, 1.0, 2.0 };
  private static final double[][] STRIKES = new double[][] {
      {50, 55, 60, 65, 70, 75, 80 },
      {50, 55, 60, 65, 70, 75, 80 },
      {50, 55, 60, 65, 70, 75, 80 },
      {40, 50, 60, 70, 80, 90 },
      {40, 50, 60, 70, 80, 90, 100 },
      {30, 50, 60, 70, 80, 90, 100 },
      {20, 40, 55, 65, 75, 90, 105, 125 } };
  private static final double[][] OTM_PRICES_FLAT;
  private static final double[][] OTM_PRICES;
  private static final SmileSurfaceDataBundle MARKET_VOLS_FLAT;
  private static final SmileSurfaceDataBundle MARKET_VOLS;
  private static final double PURE_VOL = 0.45;
  private static final PureImpliedVolatilitySurface PURE_VOL_SURFACE;
  private static final double[] TAU = new double[] {5. / 12, 11. / 12, 17. / 12, 23. / 12, 29. / 12 };
  private static final double[] ALPHA = new double[] {3.0, 2.0, 1.0, 0.0, 0.0 };
  private static final double[] BETA = new double[] {0.0, 0.02, 0.03, 0.04, 0.05 };
  private static final AffineDividends NULL_DIVIDENDS = new AffineDividends(new double[0], new double[0], new double[0]);
  private static final AffineDividends DIVIDENDS = new AffineDividends(TAU, ALPHA, BETA);

  private static final double R = 0.07;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = new YieldCurve("Discount", ConstantDoublesCurve.from(R));
  private static final VarianceSwap VS = new VarianceSwap(0, 0.75, 0.75, 0.0, 1.0, Currency.USD, 252, 252, 0, new double[0], new double[0]);
  private static final EquityVarianceSwap EVS_COR_FRO_DIVS = new EquityVarianceSwap(VS, true);
  private static final EquityVarianceSwap EVS = new EquityVarianceSwap(VS, false);

  static {
    PureImpliedVolatilitySurface flat = new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL));
    //find "market" prices if the pure implied volatility surface was flat
    OTM_PRICES_FLAT = getOptionPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);

    //set up an arbitrage free pure implied volatility surface using a mixture of log-normal model
    final int n = 3;
    final double[] sigma = new double[] {0.2, 0.5, 1.0 };
    final double[] w = new double[] {0.8, 0.15, 0.05 };
    final double[] f = new double[n];
    f[0] = 1.0;
    f[1] = 0.95;
    double sum = 0;
    for (int i = 0; i < n - 1; i++) {
      sum += w[i] * f[i];
    }
    f[n - 1] = (1.0 - sum) / w[n - 1];
    Validate.isTrue(f[n - 1] > 0);

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double expiry = tx[0];
        final double x = tx[1];
        final boolean isCall = x > 1.0;
        double price = 0;
        for (int i = 0; i < n; i++) {
          price += w[i] * BlackFormulaRepository.price(f[i], x, expiry, sigma[i], isCall);
        }
        return BlackFormulaRepository.impliedVolatility(price, 1.0, x, expiry, isCall);
      }
    };
    PURE_VOL_SURFACE = new PureImpliedVolatilitySurface(FunctionalDoublesSurface.from(surf));

    //get the 'market' prices for the hypothetical pure implied volatility surface 
    OTM_PRICES = getOptionPrices(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
    MARKET_VOLS = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, PURE_VOL_SURFACE);
    MARKET_VOLS_FLAT = getMarketVols(SPOT, EXPIRIES, STRIKES, DISCOUNT_CURVE, DIVIDENDS, flat);
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

      final EquityDividendsCurvesBundle divs = new EquityDividendsCurvesBundle(SPOT, DISCOUNT_CURVE, DIVIDENDS);

      final int n = EXPIRIES.length;
      for (int i = 0; i < n; i++) {
        double t = EXPIRIES[i];
        double f = divs.getF(t);
        double d = divs.getD(t);
        final int m = STRIKES[i].length;
        for (int j = 0; j < m; j++) {
          double k = STRIKES[i][j];
          double x = (k - d) / (f - d);
          double vol = MARKET_VOLS.getVolatilities()[i][j];
          double pVol = PURE_VOL_SURFACE.getVolatility(t, x);

          System.out.printf("%1.3f (%2.3f) & ", vol, pVol);
        }
        System.out.print("\n");
      }
    }
  }

  /**
   * This is really just testing whether we can recover the flat pure implied volatility surface from the option prices - the code paths after that point are identical 
   */
  @Test
  public void flatSurfaceTest() {
    EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
    EquityVarianceSwapStaticReplication staRep = new EquityVarianceSwapStaticReplication();
    double[] rvExpected = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, DIVIDENDS, EVS.getTimeToObsEnd(), new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL)));
    @SuppressWarnings("unused")
    double[] noDivRVExpected = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EVS.getTimeToObsEnd(), new PureImpliedVolatilitySurface(ConstantDoublesSurface.from(PURE_VOL)));

    double rv1 = pricer.priceFromOTMPrices(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);
    double rv2 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);
    double rv1PDE = pricer.priceFromLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS_FLAT);
    double rv2PDE = pricer.priceFromLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS_FLAT);
    @SuppressWarnings("unused")
    double noDivRV = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES_FLAT);

    if (PRINT) {
      System.out.println(rvExpected[0] + "\t" + rvExpected[1]);
      System.out.println(rv1 + "\t" + rv2);
      System.out.println(rv1PDE + "\t" + rv2PDE);
    }

    assertEquals("div corrected", rvExpected[0], rv1, 1e-10);
    assertEquals("div uncorrected", rvExpected[1], rv2, 1e-10);
    //we loose a lot of accuracy going via the forward PDE, but it is hardly terrible 
    //TODO investigate the source of this - it is most likely because of crude integration that is done, rather than the PDE solver per se.  
    assertEquals("div corrected PDE", rvExpected[0], rv1PDE, 1e-5);
    assertEquals("div uncorrected PDE", rvExpected[1], rv2PDE, 1e-5);

    //in this case the reconstructed implied volatility surface (which is identical to the pure one as no dividends are assumed) is NOT flat so we would not expect the items 
    //below to be equal
    //assertEquals("no divs", noDivRVExpected[0], noDivRV, 1e-10);
  }

  @Test
  //  (enabled = false)
  public void nonFlatSurfaceTest() {

    EquityVarianceSwapStaticReplication staRep = new EquityVarianceSwapStaticReplication();
    double[] rv = staRep.expectedVariance(SPOT, DISCOUNT_CURVE, DIVIDENDS, EVS.getTimeToObsEnd(), PURE_VOL_SURFACE);

    EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
    double rv1 = pricer.priceFromOTMPrices(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
    double rv2 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
    double rv3 = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, NULL_DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);

    double rv1PDE = pricer.priceFromLocalVol(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
    double rv2PDE = pricer.priceFromLocalVol(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);

    if (PRINT) {
      System.out.println(rv[0] + "\t" + rv[1]);
      System.out.println(rv1 + "\t" + rv2 + "\t" + rv3);
      System.out.println(rv1PDE + "\t" + rv2PDE);
    }

    /*
     * Starting from a finite number of market prices (albeit synthetically generated in this case), once cannot reproduce the entire (hypothetical) implied volatility
     * surface, especially at extreme strikes where there is no market information. Since expected variance depends on the entire smile (theoretically from zero to infinite strike)
     * one cannot hope to make too accurate estimate from the prices of liquid European options.   
     */
    assertEquals("div corrected", rv[0], rv1, 5e-3);
    assertEquals("div uncorrected", rv[1], rv2, 5e-3);

    // However these should agree better, as they are just two different numerical schemes using the same date  
    assertEquals("div corrected PDE", rv1, rv1PDE, 5e-4);
    assertEquals("div uncorrected PDE", rv1, rv1PDE, 5e-4);
  }

  @Test
  public void bucketVegaTest() {
    if (PRINT) {
      EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
      double[][] res = pricer.buckedVega(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      int n = res.length;
      for (int i = 0; i < n; i++) {
        int m = res[i].length;
        for (int j = 0; j < m; j++) {
          System.out.print(res[i][j] + "\t");
        }
        System.out.print("\n");
      }
    }
  }

  @Test
  public void dividendSensitivityTest() {
    if (PRINT) {
      double eps = 1e-5;
      EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
      int nDivs = TAU.length;
      double base = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, EXPIRIES, STRIKES, OTM_PRICES);
      for (int i = 0; i < nDivs; i++) {
        double[] alpha = Arrays.copyOf(ALPHA, nDivs);
        alpha[i] += (1.0 + ALPHA[i]) * eps;
        AffineDividends divs = new AffineDividends(TAU, alpha, BETA);
        double temp = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, divs, EXPIRIES, STRIKES, OTM_PRICES);
        double sense = (temp - base) / eps / (1 + ALPHA[i]);
        System.out.print(sense + "\t");
        double[] beta = Arrays.copyOf(BETA, nDivs);
        beta[i] += eps;
        divs = new AffineDividends(TAU, ALPHA, beta);
        temp = pricer.priceFromOTMPrices(EVS, SPOT, DISCOUNT_CURVE, divs, EXPIRIES, STRIKES, OTM_PRICES);
        sense = (temp - base) / eps;
        System.out.print(sense + "\n");
      }

      System.out.print("\n");
      double[][] res = pricer.dividendSensitivity2(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      for (int i = 0; i < nDivs; i++) {
        System.out.println(res[i][0] + "\t" + res[i][1]);
      }
    }
  }

  @Test
  public void deltaTest() {
    if (PRINT) {
      EquityVarianceSwapPricer pricer = new EquityVarianceSwapPricer();
      double delta1 = pricer.deltaFromImpliedVols(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta2 = pricer.deltaFromImpliedVols(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta1PDE = pricer.deltaFromLocalVols(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta2PDE = pricer.deltaFromLocalVols(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta1PDE2 = pricer.deltaFromLocalVols2(EVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta2PDE2 = pricer.deltaFromLocalVols2(EVS_COR_FRO_DIVS, SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);

      final double eps = 1e-5;
      double up = pricer.priceFromImpliedVols(EVS, (1 + eps) * SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double down = pricer.priceFromImpliedVols(EVS, (1 - eps) * SPOT, DISCOUNT_CURVE, DIVIDENDS, MARKET_VOLS);
      double delta = (up - down) / eps / SPOT;

      System.out.println(delta);
      System.out.println(delta1PDE2 + "\t" + delta2PDE2);
      System.out.println(delta1 + "\t" + delta2);
      System.out.println(delta1PDE + "\t" + delta2PDE);
    }
  }

  /**
   * Compute actual option (i.e. as would be observed in the market) from a hypothetical <b>pure</b> implied volatility surface and dividend assumptions  
   * @param spot The current level of the stock or index
   * @param expiries expiries of option strips 
   * @param strikes strikes at each expiry 
   * @param discountCurve The discount curve 
   * @param dividends dividend assumptions 
   * @param surf hypothetical <b>pure</b> implied volatility surface
   * @return Market observed option prices 
   */
  private static double[][] getOptionPrices(final double spot, final double[] expiries, final double[][] strikes, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final PureImpliedVolatilitySurface surf) {
    int nExp = expiries.length;
    double[][] prices = new double[nExp][];
    EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    for (int i = 0; i < nExp; i++) {
      final int n = strikes[i].length;
      prices[i] = new double[n];
      double t = expiries[i];
      double f = divCurves.getF(t);
      double d = divCurves.getD(t);
      double p = discountCurve.getDiscountFactor(t);
      for (int j = 0; j < n; j++) {
        double x = (strikes[i][j] - d) / (f - d);
        boolean isCall = x >= 1.0;
        double vol = surf.getVolatility(t, x);
        prices[i][j] = p * (f - d) * BlackFormulaRepository.price(1.0, x, t, vol, isCall);
      }
    }
    return prices;
  }

  private static SmileSurfaceDataBundle getMarketVols(final double spot, final double[] expiries, final double[][] strikes, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final PureImpliedVolatilitySurface surf) {
    int nExp = expiries.length;
    double[] f = new double[nExp];
    double[][] vols = new double[nExp][];
    EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    for (int i = 0; i < nExp; i++) {
      final int n = strikes[i].length;
      vols[i] = new double[n];
      double t = expiries[i];
      f[i] = divCurves.getF(t);
      double d = divCurves.getD(t);
      for (int j = 0; j < n; j++) {
        double x = (strikes[i][j] - d) / (f[i] - d);
        boolean isCall = x >= 1.0;
        double pVol = surf.getVolatility(t, x);
        double p = (f[i] - d) * BlackFormulaRepository.price(1.0, x, t, pVol, isCall);
        vols[i][j] = BlackFormulaRepository.impliedVolatility(p, f[i], strikes[i][j], t, pVol);
      }
    }
    return new StandardSmileSurfaceDataBundle(spot, f, expiries, strikes, vols, Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE);
  }

}
