/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanSpreadOptionFunctionProviderTest {

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTri = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {1., 5., 14. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {0.017, 0.05 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.014 };

  /**
   * 
   */
  @Test
  public void binomialTrinomialTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 58;
    final int nStepsTri = 19;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double strike : STRIKES) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  final OptionFunctionProvider2D functionTri = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nStepsTri, isCall);
                  final double resDivTri = _modelTri.getPrice(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertEquals(resDivTri, resDiv, resDiv * 1.e-2);

                  final double[] greek = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  final double[] greekTri = _modelTri.getGreeks(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertGreeks(greekTri, greek, 1.e-1);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Analytic formula is available if K =0
   */
  @Test
  public void priceZeroStrikeTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 58;

    final double strike = 0.;
    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                double exactDiv = BlackScholesFormulaRepository.price(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double refDiv = Math.max(exactDiv, 1.) * 1.e-2;
                assertEquals(resDiv, exactDiv, refDiv);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Reduces into Black-Scholes formula if spot 2 = 0.
   */
  @Test
  public void priceZeroSpot2Test() {
    final double spot2 = 1.e-10;
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 188;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : STRIKES) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double refDiv = Math.max(exactDiv, 1.) * 1.e-2;
                assertEquals(resDiv, exactDiv, refDiv);
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
  public void greeksZeroStrikeTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 78;

    final double strike = 0.;
    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double price = BlackScholesFormulaRepository.price(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                final double delta1 = BlackScholesFormulaRepository.delta(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                final double delta2 = BlackScholesFormulaRepository.dualDelta(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                final double theta = BlackScholesFormulaRepository.theta(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                final double gamma1 = BlackScholesFormulaRepository.gamma(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend);
                final double gamma2 = BlackScholesFormulaRepository.dualGamma(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend);
                final double cross = BlackScholesFormulaRepository.crossGamma(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend);
                final double[] ref = new double[] {price, delta1, delta2, theta, gamma1, gamma2, cross };
                final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                assertGreeks(res, ref, 1.e-2);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Reduces into Black-Scholes formula if spot 2 = 0.
   */
  @Test
  public void greekZeroSpot2Test() {
    final double spot2 = 1.e-10;
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 269;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : STRIKES) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double price = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double delta1 = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double theta = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double gamma1 = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                final double[] ref = new double[] {price, delta1, theta, gamma1 };
                final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double[] resMod = new double[] {res[0], res[1], res[3], res[4] };
                //                System.out.println(resMod[3] + "\t" + ref[3]);
                assertGreeks(resMod, ref, 1.e-2);
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
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeTest() {
    new EuropeanSpreadOptionFunctionProvider(-10., TIME, 202, true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeTest() {
    new EuropeanSpreadOptionFunctionProvider(10., -TIME, 202, true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fewStepsTest() {
    new EuropeanSpreadOptionFunctionProvider(10., TIME, 2, true);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true),
        new EuropeanSpreadOptionFunctionProvider(100., 1., 53, false), new EuropeanSpreadOptionFunctionProvider(110., 1., 53, true), new EuropeanSpreadOptionFunctionProvider(100., 2., 53, true),
        new EuropeanSpreadOptionFunctionProvider(100., 2., 54, true), new AmericanSpreadOptionFunctionProvider(100., 1., 53, true), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanVanillaOptionFunctionProvider(100., 1., 53, true)));
  }

  private void assertGreeks(final double[] res, final double[] ref, final double eps) {
    final int size = res.length;
    ArgumentChecker.isTrue(size == ref.length, "wrong data length");
    for (int i = 0; i < size; ++i) {
      //      System.out.println(i);
      final double error = Math.max(Math.abs(ref[i]), 1.) * eps;
      assertEquals(res[i], ref[i], error);
    }
  }

}
