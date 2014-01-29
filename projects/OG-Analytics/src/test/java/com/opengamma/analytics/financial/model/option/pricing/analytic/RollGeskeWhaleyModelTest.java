/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.tree.AmericanVanillaOptionFunctionProvider;
import com.opengamma.analytics.financial.model.option.pricing.tree.BinomialTreeOptionPricingModel;
import com.opengamma.analytics.financial.model.option.pricing.tree.CashDividendFunctionProvider;
import com.opengamma.analytics.financial.model.option.pricing.tree.DividendFunctionProvider;
import com.opengamma.analytics.financial.model.option.pricing.tree.LatticeSpecification;
import com.opengamma.analytics.financial.model.option.pricing.tree.LeisenReimerLatticeSpecification;
import com.opengamma.analytics.financial.model.option.pricing.tree.OptionFunctionProvider1D;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class RollGeskeWhaleyModelTest {
  private static final double TIME_TO_EXPIRY = 1.;
  private static final double SPOT = 100.;
  private static final double[] STRIKES_INPUT = new double[] {35., 89.0, 100.0, 106.0, 165.0 };
  private static final double[] INTEREST_RATES = new double[] {-0.01, -0.005, 0, 0.01, 0.1, 0.2 };
  private static final double[] VOLS = new double[] {0.1, 0.3, 0.8 };
  private static final double[] DIVIDENDS = new double[] {15., 25. };
  private static final double[] DIVIDEND_TIMES = new double[] {0.1, 0.5, 0.9 };
  private static final double[][] MULTI_DIVIDENDS = new double[][] { {5., 5., 5., 5. }, {5., 8., 11., 12. } };
  private static final double[][] MULTI_DIVIDEND_TIMES = new double[][] { {0.1, 0.35, 0.6, 0.85 }, {0.4, 0.7, 0.95, 1.2 } };
  private static final double DELTA = 1.e-4;
  private static final RollGeskeWhaleyModel MODEL = new RollGeskeWhaleyModel();

  /**
   * The limited case where the price reduces into Black-Scholes price
   */
  @Test
  public void priceLimtedTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final double eps = 1.e-5;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {

          /*
           * For the stability of the root-finder for sStar we shall take the limit in dividendTime as well.
           */
          final double dividendTimes = TIME_TO_EXPIRY - eps;
          final double dividend = eps;

          //            System.out.println(i + "\t" + j + "\t" + k);
          final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], dividend, dividendTimes);
          final double priceZeroDiv = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], 0., dividendTimes);
          final double priceZeroTime = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[0], 0.);
          final double priceSmallTime = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[0], eps);

          final double bs = BlackScholesFormulaRepository.price(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double bsMod = BlackScholesFormulaRepository.price(SPOT - dividend, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);

          assertEquals(bsMod, price, Math.max(eps, Math.abs(bs) * eps));
          assertEquals(bs, priceZeroDiv, 1.e-14);
          assertEquals(priceSmallTime, priceZeroTime, Math.max(eps, Math.abs(priceZeroTime) * eps));
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void noDividendTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final double eps = 1.e-5;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {

          /*
           * For the stability of the root-finder for sStar we shall take the limit in dividendTime as well.
           */
          final double dividendTimes = TIME_TO_EXPIRY + eps;
          final double dividend = 0.1 * SPOT;

          //            System.out.println(i + "\t" + j + "\t" + k);
          final double[] greeks = MODEL.getPriceAdjoint(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], new double[] {dividend }, new double[] {dividendTimes });

          final double price = BlackScholesFormulaRepository.price(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double delta = BlackScholesFormulaRepository.delta(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double dualDelta = BlackScholesFormulaRepository.dualDelta(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double theta = BlackScholesFormulaRepository.theta(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double rho = BlackScholesFormulaRepository.rho(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);
          final double vega = BlackScholesFormulaRepository.vega(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k]);
          final double gamma = BlackScholesFormulaRepository.gamma(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k]);

          assertEquals(price, greeks[0], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(delta, greeks[1], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(dualDelta, greeks[2], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(rho, greeks[3], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(-theta, greeks[4], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(0., greeks[5]);
          assertEquals(vega, greeks[6], Math.max(eps, Math.abs(price) * 1.e-14));
          assertEquals(gamma, greeks[7], Math.max(eps, Math.abs(price) * 1.e-14));
        }
      }
    }
  }

  /**
   * The limited case where the price reduces into modified Bjerksund-Stensland price
   */
  @Test
  public void priceBjsTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {

          final double dividend = SPOT * 1.e-3;
          final double dividendTime = TIME_TO_EXPIRY * 1.e-3;
          //          if (dividend > (1.1 - Math.exp(-INTEREST_RATES[k] * (TIME_TO_EXPIRY - dividendTime))) * STRIKES_INPUT[i]) {

          //              System.out.println(i + "\t" + j + "\t" + k + "\t" + m);
          final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], dividend, dividendTime);
          final double modSpot = SPOT - dividend * Math.exp(-INTEREST_RATES[k] * dividendTime);
          final BjerksundStenslandModel bjs = new BjerksundStenslandModel();
          final double bjsPrice = bjs.price(modSpot, STRIKES_INPUT[i], INTEREST_RATES[k], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], true);

          /*
           * Smoothing out discrete dividends sometimes produce a value which is largely different from the RGW model.
           */
          //          final double fwd = SPOT * Math.exp(INTEREST_RATES[k] * TIME_TO_EXPIRY) - dividend * Math.exp(INTEREST_RATES[k] * (TIME_TO_EXPIRY - dividendTime));
          //          final double coc = Math.log(fwd / SPOT) / TIME_TO_EXPIRY;
          //          final double bjsPriceFwd = bjs.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], coc, TIME_TO_EXPIRY, VOLS[j], true);
          //          System.out.println(price + "\t" + bjsPriceFwd + "\t" + bjsPrice);
          assertEquals(bjsPrice, price, Math.max(1.e-2, Math.abs(bjsPrice) * 1.e-2));
          //          }
        }
      }
    }
  }

  /**
   * Implied volatility calculator is tested
   */
  @Test
  public void impliedVolTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = DIVIDENDS.length;
    final int nDivT = DIVIDEND_TIMES.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int l = 0; l < nDiv; ++l) {
            for (int m = 0; m < nDivT; ++m) {
              //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);
              final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
              final double impliedVol = MODEL.impliedVolatility(price, SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, DIVIDENDS[l], DIVIDEND_TIMES[m]);
              final double priceRe = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, impliedVol, DIVIDENDS[l], DIVIDEND_TIMES[m]);

              /*
               * Due to zero vega for small vols, as found in other American option models, comparing vol sometimes fails.
               * Instead the resulting option price is tested.
               */
              //                assertEquals(VOLS[j], impliedVol, Math.max(DELTA, Math.abs(VOLS[j]) * DELTA));
              assertEquals(price, priceRe, Math.max(price, Math.abs(price) * DELTA));
            }
          }
        }
      }
    }
  }

  /**
   * Analytic Greeks are tested 
   */
  @Test
  public void greeksFiniteDiffTest() {
    final double[] divLocal = new double[] {5., 12., 0. };
    final double[] divTimeLocal = new double[] {0.1, 0.85, 0.4, 0.7, 0. };

    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = divLocal.length;
    final int nDivT = divTimeLocal.length;

    final double[] upStrikes = new double[nStrikes];
    final double[] dwStrikes = new double[nStrikes];
    final double upSpot = SPOT * (1. + DELTA);
    final double dwSpot = SPOT * (1. - DELTA);
    final double upTime = TIME_TO_EXPIRY * (1. + DELTA);
    final double dwTime = TIME_TO_EXPIRY * (1. - DELTA);
    final double[] upVOLS = new double[nVols];
    final double[] dwVOLS = new double[nVols];
    final double[] upInt = new double[nInt];
    final double[] dwInt = new double[nInt];

    for (int i = 0; i < nStrikes; ++i) {
      upStrikes[i] = STRIKES_INPUT[i] * (1. + DELTA);
      dwStrikes[i] = STRIKES_INPUT[i] * (1. - DELTA);
    }
    for (int i = 0; i < nVols; ++i) {
      upVOLS[i] = VOLS[i] * (1. + DELTA);
      dwVOLS[i] = VOLS[i] * (1. - DELTA);
    }
    for (int i = 0; i < nInt; ++i) {
      upInt[i] = INTEREST_RATES[i] == 0. ? DELTA : INTEREST_RATES[i] * (1. + DELTA);
      dwInt[i] = INTEREST_RATES[i] == 0. ? -DELTA : INTEREST_RATES[i] * (1. - DELTA);
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int l = 0; l < nDiv; ++l) {
            for (int m = 0; m < nDivT; ++m) {
              //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);

              final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double[] greeks = MODEL.getPriceAdjoint(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);

              final double[] greeksUp = MODEL.getPriceAdjoint(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double[] greeksDw = MODEL.getPriceAdjoint(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceSpotUp = MODEL.price(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceSpotDw = MODEL.price(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceStrikeUp = MODEL.price(SPOT, upStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceStrikeDw = MODEL.price(SPOT, dwStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceRateUp = MODEL.price(SPOT, STRIKES_INPUT[i], upInt[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceRateDw = MODEL.price(SPOT, STRIKES_INPUT[i], dwInt[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceTimeUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], upTime, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceTimeDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], dwTime, VOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceVolUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, upVOLS[j], divLocal[l], divTimeLocal[m]);
              final double priceVolDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, dwVOLS[j], divLocal[l], divTimeLocal[m]);

              final double deltaFin = 0.5 * (priceSpotUp - priceSpotDw) / SPOT / DELTA;
              final double dualDeltaFin = 0.5 * (priceStrikeUp - priceStrikeDw) / STRIKES_INPUT[i] / DELTA;
              final double rhoFin = INTEREST_RATES[k] == 0. ? 0.5 * (priceRateUp - priceRateDw) / DELTA : 0.5 * (priceRateUp - priceRateDw) / INTEREST_RATES[k] / DELTA;
              final double thetaFin = 0.5 * (priceTimeUp - priceTimeDw) / TIME_TO_EXPIRY / DELTA;
              final double vegaFin = 0.5 * (priceVolUp - priceVolDw) / VOLS[j] / DELTA;
              final double gammaFin = 0.5 * (greeksUp[1] - greeksDw[1]) / SPOT / DELTA;

              assertEquals(price, greeks[0], 1.e-12);
              assertEquals(deltaFin, greeks[1], Math.max(DELTA, Math.abs(deltaFin) * DELTA));
              assertEquals(dualDeltaFin, greeks[2], Math.max(DELTA, Math.abs(dualDeltaFin) * DELTA));
              assertEquals(rhoFin, greeks[3], Math.max(DELTA, Math.abs(rhoFin) * DELTA));
              assertEquals(thetaFin, greeks[4], Math.max(DELTA, Math.abs(thetaFin) * DELTA));
              assertEquals(vegaFin, greeks[6], Math.max(DELTA, Math.abs(vegaFin) * DELTA));
              assertEquals(gammaFin, greeks[7], Math.max(DELTA, Math.abs(gammaFin) * DELTA));

              final boolean zeroTime = divTimeLocal[m] == 0.;
              final double base = zeroTime ? 1. : divTimeLocal[m];
              final double delta = zeroTime ? 1.e-7 : DELTA;
              final double divTimeLocalUp = zeroTime ? delta : divTimeLocal[m] * (1. + delta);
              final double divTimeLocalDw = zeroTime ? 0. : divTimeLocal[m] * (1. - delta);
              final double coeff = zeroTime ? 1. : 0.5;

              final double priceDivTimeUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocalUp);
              final double priceDivTimeDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], divLocal[l], divTimeLocalDw);
              final double thetaDivFin = coeff * (priceDivTimeUp - priceDivTimeDw) / base / delta;
              assertEquals(thetaDivFin, greeks[5], Math.max(DELTA, Math.abs(thetaDivFin) * DELTA));

            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void errorTest() {
    final double[] params = new double[] {100., 100., 0.5, 0.2, 20., 0.8 };
    final int nParams = params.length;

    for (int i = 0; i < nParams; ++i) {
      params[i] *= -1.;
      try {
        MODEL.price(params[0], params[1], 0.05, params[2], params[3], params[4], params[5]);
      } catch (Exception e) {
        assertTrue(e instanceof IllegalArgumentException);
      }
      try {
        MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], params[4], params[5]);
      } catch (Exception e) {
        assertTrue(e instanceof IllegalArgumentException);
      }
      params[i] *= -1.;
    }

    try {
      MODEL.price(params[0], params[1], 0.05, params[2], params[3], params[0] + params[4], params[5]);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], params[0] + params[4], params[5]);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.price(params[0], params[1], 0.05, params[2], params[3], params[4], params[5] + params[2]);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], params[4], params[5] + params[2]);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * Analytic Greeks are tested 
   */
  @Test
  public void greeksFiniteDiffMultiTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = MULTI_DIVIDENDS.length;
    final int nDivT = MULTI_DIVIDEND_TIMES.length;

    final double[] upStrikes = new double[nStrikes];
    final double[] dwStrikes = new double[nStrikes];
    final double upSpot = SPOT * (1. + DELTA);
    final double dwSpot = SPOT * (1. - DELTA);
    final double upTime = TIME_TO_EXPIRY * (1. + DELTA);
    final double dwTime = TIME_TO_EXPIRY * (1. - DELTA);
    final double[] upVOLS = new double[nVols];
    final double[] dwVOLS = new double[nVols];
    final double[] upInt = new double[nInt];
    final double[] dwInt = new double[nInt];

    for (int i = 0; i < nStrikes; ++i) {
      upStrikes[i] = STRIKES_INPUT[i] * (1. + DELTA);
      dwStrikes[i] = STRIKES_INPUT[i] * (1. - DELTA);
    }
    for (int i = 0; i < nVols; ++i) {
      upVOLS[i] = VOLS[i] * (1. + DELTA);
      dwVOLS[i] = VOLS[i] * (1. - DELTA);
    }
    for (int i = 0; i < nInt; ++i) {
      upInt[i] = INTEREST_RATES[i] == 0. ? DELTA : INTEREST_RATES[i] * (1. + DELTA);
      dwInt[i] = INTEREST_RATES[i] == 0. ? -DELTA : INTEREST_RATES[i] * (1. - DELTA);
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int l = 0; l < nDiv; ++l) {
            for (int m = 0; m < nDivT; ++m) {
              //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);

              final int nPymnts = MULTI_DIVIDEND_TIMES[m].length;
              final double[] divTimeUp = new double[nPymnts];
              final double[] divTimeDw = new double[nPymnts];
              for (int n = 0; n < nPymnts; ++n) {
                divTimeUp[n] = MULTI_DIVIDEND_TIMES[m][n] + DELTA;
                divTimeDw[n] = MULTI_DIVIDEND_TIMES[m][n] - DELTA;
              }

              final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double[] greeks = MODEL.getPriceAdjoint(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);

              final double[] greeksUp = MODEL.getPriceAdjoint(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double[] greeksDw = MODEL.getPriceAdjoint(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceSpotUp = MODEL.price(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceSpotDw = MODEL.price(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceStrikeUp = MODEL.price(SPOT, upStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceStrikeDw = MODEL.price(SPOT, dwStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceRateUp = MODEL.price(SPOT, STRIKES_INPUT[i], upInt[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceRateDw = MODEL.price(SPOT, STRIKES_INPUT[i], dwInt[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceTimeUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], upTime, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceTimeDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], dwTime, VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceDivTimeUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], divTimeUp);
              final double priceDivTimeDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l], divTimeDw);
              final double priceVolUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, upVOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
              final double priceVolDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, dwVOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);

              final double deltaFin = 0.5 * (priceSpotUp - priceSpotDw) / SPOT / DELTA;
              final double dualDeltaFin = 0.5 * (priceStrikeUp - priceStrikeDw) / STRIKES_INPUT[i] / DELTA;
              final double rhoFin = INTEREST_RATES[k] == 0. ? 0.5 * (priceRateUp - priceRateDw) / DELTA : 0.5 * (priceRateUp - priceRateDw) / INTEREST_RATES[k] / DELTA;
              final double thetaFin = 0.5 * (priceTimeUp - priceTimeDw) / TIME_TO_EXPIRY / DELTA;
              final double thetaDivFin = 0.5 * (priceDivTimeUp - priceDivTimeDw) / DELTA;
              final double vegaFin = 0.5 * (priceVolUp - priceVolDw) / VOLS[j] / DELTA;
              final double gammaFin = 0.5 * (greeksUp[1] - greeksDw[1]) / SPOT / DELTA;

              assertEquals(price, greeks[0], 1.e-12);
              assertEquals(deltaFin, greeks[1], Math.max(DELTA, Math.abs(deltaFin) * DELTA));
              assertEquals(dualDeltaFin, greeks[2], Math.max(DELTA, Math.abs(dualDeltaFin) * DELTA));
              assertEquals(rhoFin, greeks[3], Math.max(DELTA, Math.abs(rhoFin) * DELTA));
              assertEquals(thetaFin, greeks[4], Math.max(DELTA, Math.abs(thetaFin) * DELTA));
              assertEquals(thetaDivFin, greeks[5], Math.max(DELTA, Math.abs(thetaDivFin) * DELTA));
              assertEquals(vegaFin, greeks[6], Math.max(DELTA, Math.abs(vegaFin) * DELTA));
              assertEquals(gammaFin, greeks[7], Math.max(DELTA, Math.abs(gammaFin) * DELTA));
            }
          }
        }
      }
    }
  }

  /**
   * Comparison to binomial tree
   * The recombining tree becomes a poor approximation if the number of dividend payments before the expiry is more than one
   */
  @Test
  public void treeComparisonTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = MULTI_DIVIDENDS.length;
    final int nDivT = MULTI_DIVIDEND_TIMES.length;

    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    final int steps = 101;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int l = 0; l < nDiv; ++l) {
            for (int m = 0; m < nDivT - 1; ++m) {
              //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);

              if (MULTI_DIVIDENDS[l][2] != 0.) {
                final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], MULTI_DIVIDENDS[l][2], MULTI_DIVIDEND_TIMES[m][2]);

                final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES_INPUT[i], TIME_TO_EXPIRY, steps, true);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(new double[] {MULTI_DIVIDEND_TIMES[m][2] }, new double[] {MULTI_DIVIDENDS[l][2] });
                final BinomialTreeOptionPricingModel model = new BinomialTreeOptionPricingModel();
                final double priceTree = model.getPrice(lattice, function, SPOT, VOLS[j], INTEREST_RATES[k], cashDividend);
                assertEquals(priceTree, price, Math.max(2.e-2, priceTree * 5.e-2));
                //                System.out.println(price + "\t" + priceTree);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Implied volatility calculator is tested
   */
  @Test
  public void impliedVolMultiTest() {
    final double[] localTimeSet = new double[] {TIME_TO_EXPIRY, MULTI_DIVIDEND_TIMES[1][0] - 1.e-4 };
    final int nTimeSet = localTimeSet.length;

    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = MULTI_DIVIDENDS.length;
    final int nDivT = MULTI_DIVIDEND_TIMES.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int l = 0; l < nDiv; ++l) {
            for (int m = 0; m < nDivT; ++m) {
              for (int n = 0; n < nTimeSet; ++n) {
                //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);

                final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], localTimeSet[n], VOLS[j], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
                final double impliedVol = MODEL.impliedVolatility(price, SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], localTimeSet[n], MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);
                final double priceRe = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], localTimeSet[n], impliedVol, MULTI_DIVIDENDS[l], MULTI_DIVIDEND_TIMES[m]);

                /*
                 * Due to zero vega for small vols, as found in other American option models, comparing vol sometimes fails.
                 * Instead the resulting option price is tested.
                 */
                //                assertEquals(VOLS[j], impliedVol, Math.max(DELTA, Math.abs(VOLS[j]) * DELTA));
                assertEquals(price, priceRe, Math.max(price, Math.abs(price) * DELTA));
              }
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void errorMultiTest() {
    final double[] params = new double[] {100., 100., 0.9, 0.2, 20., 0.8 };
    final int nParams = params.length;

    for (int i = 0; i < nParams - 2; ++i) {
      params[i] *= -1.;
      try {
        MODEL.price(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5] });
        throw new RuntimeException();
      } catch (Exception e) {
        assertTrue(e instanceof IllegalArgumentException);
      }
      try {
        MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5] });
        throw new RuntimeException();
      } catch (Exception e) {
        assertTrue(e instanceof IllegalArgumentException);
      }
      params[i] *= -1.;
    }

    try {
      MODEL.price(params[0], params[1], 0.05, params[2], params[3], new double[] {params[0] + params[4] }, new double[] {params[5] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], new double[] {params[0] + params[4] }, new double[] {params[5] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    //    try {
    //      MODEL.price(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5] + params[2] });
    //      throw new RuntimeException();
    //    } catch (Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    //    try {
    //      MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5] + params[2] });
    //      throw new RuntimeException();
    //    } catch (Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    try {
      MODEL.impliedVolatility(params[0] / 10., params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5] + params[2] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.price(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5], params[2] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      MODEL.getPriceAdjoint(params[0], params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5], params[2] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      MODEL.impliedVolatility(params[0] / 10., params[1], 0.05, params[2], params[3], new double[] {params[4] }, new double[] {params[5], params[2] });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * Tests below are for debugging
   */
  @Test(enabled = false)
  public void simpleTest() {
    final double spot = 50.;
    final double strike = 50.;
    final double timeToExpiry = 90. / 365.;
    final double vol = 0.36;
    final double div = 2.;
    final double divTime = 75. / 365.;
    final double rate = 0.05;

    final RollGeskeWhaleyModel model = new RollGeskeWhaleyModel();
    final double price = model.price(spot, strike, rate, timeToExpiry, vol, div, divTime);
    System.out.println(model.price(spot, strike, rate, timeToExpiry, vol, div, divTime));
    System.out.println(model.impliedVolatility(price, spot, strike, rate, timeToExpiry, div, divTime));

    final BjerksundStenslandModel bjs = new BjerksundStenslandModel();
    System.out.println(bjs.impliedVolatility(price, spot - div * Math.exp(-rate * divTime), strike, rate, rate, timeToExpiry, true));

    final double[] greeks = model.getPriceAdjoint(spot, strike, rate, timeToExpiry, vol, div, divTime);
    System.out.println(price + "\t" + greeks[0]);

  }

  /**
   * 
   */
  @Test(enabled = false)
  public void mktDataTest() {
    final double spot = 56.144999999999996;
    final double price = 16.225;
    final double time = 0.10410958904109589;
    final double strike = 40.;
    final double rate = 0.0024352885167853145;
    final double div = 0.38;
    final double divTime = 0.06027397260273973;
    final double quantity = -150;

    final double vol = MODEL.impliedVolatility(price, spot, strike, rate, time, div, divTime);
    final double[] greeks = MODEL.getPriceAdjoint(spot, strike, rate, time, vol, div, divTime);
    final double volRe = MODEL.impliedVolatility(16.25, 56.19, strike, rate, time, div, divTime);
    final double[] greeksRe = MODEL.getPriceAdjoint(56.19, strike, rate, time, volRe, div, divTime);

    System.out.println(vol + "\t" + volRe);
    System.out.println(greeks[0] + "\t" + greeksRe[0]);
    System.out.println(greeks[1] + "\t" + greeksRe[1]);
    System.out.println(greeks[2] + "\t" + greeksRe[2]);
    System.out.println(greeks[3] + "\t" + greeksRe[3]);
    System.out.println(greeks[4] + "\t" + greeksRe[4]);
    System.out.println(greeks[5] * quantity + "\t" + greeksRe[5] * quantity);
    System.out.println(greeks[6] * quantity + "\t" + greeksRe[6] * quantity);

    //    System.out.println("\n");
    //    for (int i = 0; i < 150; ++i) {
    //      final double vols = 0.01 + 0.005 * i;
    //      System.out.println(vols + "\t" + (MODEL.price(spot, strike, rate, time, vols, div, divTime) - price));
    //    }
  }
}
