/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;

/**
 * 
 */
public class RollGeskeWhaleyModelTest {
  private static final double TIME_TO_EXPIRY = 1.;
  private static final double SPOT = 100.;
  private static final double[] STRIKES_INPUT = new double[] {35., 85.0, 95.0, 100.0, 103.0, 120.0, 165.0 };
  private static final double[] INTEREST_RATES = new double[] {-0.01, -0.005, 0, 0.01, 0.1, 0.2 };
  private static final double[] VOLS = new double[] {0.1, 0.14, 0.3, 0.8 };
  private static final double[] DIVIDENDS = new double[] {15., 25. };
  private static final double[] DIVIDEND_TIMES = new double[] {0.1, 0.5, 0.9 };
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
    final int nDivT = DIVIDEND_TIMES.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        for (int k = 0; k < nInt; ++k) {
          for (int m = 0; m < nDivT; ++m) {
            //              System.out.println(i + "\t" + j + "\t" + k + "\t" + l + "\t" + m);

            final double dividend = 1.e-2;
            if (dividend > (1.0 - Math.exp(-INTEREST_RATES[k] * TIME_TO_EXPIRY)) * STRIKES_INPUT[i]) {

              final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], dividend, DIVIDEND_TIMES[m]);
              final double bs = BlackScholesFormulaRepository.price(SPOT, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], INTEREST_RATES[k], INTEREST_RATES[k], true);

              assertEquals(bs, price, Math.max(bs, Math.abs(bs) * DELTA));
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

              if (DIVIDENDS[l] > (1.01 - Math.exp(-INTEREST_RATES[k] * TIME_TO_EXPIRY)) * STRIKES_INPUT[i]) {

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
  }

  /**
   * Analytic Greeks are tested 
   */
  @Test
  public void greeksFiniteDiffTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final int nInt = INTEREST_RATES.length;
    final int nDiv = DIVIDENDS.length;
    final int nDivT = DIVIDEND_TIMES.length;

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

              if (DIVIDENDS[l] > (1. - Math.exp(-INTEREST_RATES[k] * TIME_TO_EXPIRY)) * STRIKES_INPUT[i]) {

                final double price = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double[] greeks = MODEL.getPriceAdjoint(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);

                final double[] greeksUp = MODEL.getPriceAdjoint(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double[] greeksDw = MODEL.getPriceAdjoint(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceSpotUp = MODEL.price(upSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceSpotDw = MODEL.price(dwSpot, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceStrikeUp = MODEL.price(SPOT, upStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceStrikeDw = MODEL.price(SPOT, dwStrikes[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceRateUp = MODEL.price(SPOT, STRIKES_INPUT[i], upInt[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceRateDw = MODEL.price(SPOT, STRIKES_INPUT[i], dwInt[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceTimeUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], upTime, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceTimeDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], dwTime, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceVolUp = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, upVOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                final double priceVolDw = MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, dwVOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);

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
                assertEquals(thetaFin, greeks[4], Math.max(DELTA, Math.abs(thetaFin) * DELTA) * 10.);
                assertEquals(vegaFin, greeks[5], Math.max(DELTA, Math.abs(vegaFin) * DELTA));
                assertEquals(gammaFin, greeks[6], Math.max(DELTA, Math.abs(gammaFin) * DELTA));
              } else {
                try {
                  MODEL.price(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                } catch (Exception e) {
                  assertTrue(e instanceof IllegalArgumentException);
                }
                try {
                  MODEL.getPriceAdjoint(SPOT, STRIKES_INPUT[i], INTEREST_RATES[k], TIME_TO_EXPIRY, VOLS[j], DIVIDENDS[l], DIVIDEND_TIMES[m]);
                } catch (Exception e) {
                  assertTrue(e instanceof IllegalArgumentException);
                }
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
   * Tests below are for debugging
   */
  @Test(enabled = false)
  public void test() {
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
  public void testa() {
    final double spot = 140.;
    final double strike = 150.;
    final double timeToExpiry = 10. / 365.;
    final double volatility = 0.36;
    final double dividendAmount = 2.;
    final double dividendTime = 8. / 365.;
    final double interestRate = 0.05;
    final RollGeskeWhaleyModel model = new RollGeskeWhaleyModel();

    final double eps = 1.e-6;

    final double spotUp = spot + eps;
    final double spotDw = spot - eps;
    final double strikeUp = strike + eps;
    final double strikeDw = strike - eps;
    final double interestRateUp = interestRate + eps;
    final double interestRateDw = interestRate - eps;
    final double timeToExpiryUp = timeToExpiry + eps;
    final double timeToExpiryDw = timeToExpiry - eps;
    final double volatilityUp = volatility + eps * 0.01;
    final double volatilityDw = volatility - eps * 0.01;

    final double price = model.price(spot, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceSpotUp = model.price(spotUp, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceSpotDw = model.price(spotDw, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceStrikeUp = model.price(spot, strikeUp, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceStrikeDw = model.price(spot, strikeDw, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceRateUp = model.price(spot, strike, interestRateUp, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceRateDw = model.price(spot, strike, interestRateDw, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double priceTimeUp = model.price(spot, strike, interestRate, timeToExpiryUp, volatility, dividendAmount, dividendTime);
    final double priceTimeDw = model.price(spot, strike, interestRate, timeToExpiryDw, volatility, dividendAmount, dividendTime);
    final double priceVolUp = model.price(spot, strike, interestRate, timeToExpiry, volatilityUp, dividendAmount, dividendTime);
    final double priceVolDw = model.price(spot, strike, interestRate, timeToExpiry, volatilityDw, dividendAmount, dividendTime);

    final double[] greeks = model.getPriceAdjoint(spot, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double[] greeksUp = model.getPriceAdjoint(spotUp, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);
    final double[] greeksDw = model.getPriceAdjoint(spotDw, strike, interestRate, timeToExpiry, volatility, dividendAmount, dividendTime);

    System.out.println(price + "\t" + greeks[0]);
    System.out.println(0.5 * (priceSpotUp - priceSpotDw) / eps + "\t" + greeks[1]);
    System.out.println(0.5 * (priceStrikeUp - priceStrikeDw) / eps + "\t" + greeks[2]);
    System.out.println(0.5 * (priceRateUp - priceRateDw) / eps + "\t" + greeks[3]);
    System.out.println(0.5 * (priceTimeUp - priceTimeDw) / eps + "\t" + greeks[4]);
    System.out.println(0.5 * (priceVolUp - priceVolDw) / eps / 0.01 + "\t" + greeks[5]);
    System.out.println(0.5 * (greeksUp[1] - greeksDw[1]) / eps + "\t" + greeks[6]);
  }

  /**
   * 
   */
  @Test(enabled = false)
  public void testb() {
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
