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

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AmericanSpreadOptionFunctionProviderTest {

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
    final double[] spot2Set = new double[] {100., 110. };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {0.4 };
    final int nSteps = 89;
    final int nStepsTri = 11;
    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : STRIKES) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                for (final double spot2 : spot2Set) {
                  final OptionFunctionProvider2D function2D = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double res = _model.getPrice(function2D, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  final OptionFunctionProvider2D functionTri = new AmericanSpreadOptionFunctionProvider(strike, TIME, nStepsTri, isCall);
                  final double resDivTri = _modelTri.getPrice(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertEquals(resDivTri, res, res * 1.e-2);

                  final double[] greek = _model.getGreeks(function2D, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
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
   * Reduces into American vanilla if spot 2 = 0.
   */
  @Test
  public void priceZeroSpot2Test() {
    final double spot2 = 1.e-10;
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {0.4 };
    final int nSteps = 89;
    final double[] strikes = new double[] {100., 110. };
    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : strikes) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function2D = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final OptionFunctionProvider1D function1D = new AmericanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double res2D = _model.getPrice(function2D, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double res1D = _model.getPrice(new LeisenReimerLatticeSpecification(), function1D, SPOT, vol, interest, dividend);
                final double ref = Math.max(res1D, 1.) * 1.e-2;
                assertEquals(res2D, res1D, ref);
              }
            }
          }
        }
      }
    }
  }

  /**
   * s1 \equiv s2
   */
  @Test
  public void priceSameSpotsTest() {
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {0.4 };
    final int nSteps = 89;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : STRIKES) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D functionAmerican = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final OptionFunctionProvider2D functionEuropean = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double resAmerican = _model.getPrice(functionAmerican, SPOT, SPOT, vol, sigma2, rho, interest, dividend, dividend);
                final double resEuropean = _model.getPrice(functionEuropean, SPOT, SPOT, vol, sigma2, rho, interest, dividend, dividend);
                final double ref = Math.max(resEuropean, 1.) * 1.e-1;
                assertEquals(resAmerican, resEuropean, ref);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Reduces into American vanilla if spot 2 = 0.
   */
  @Test
  public void greekZeroSpot2Test() {
    final double spot2 = 1.e-10;
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {0.4 };
    final double[] strikes = new double[] {100., 110. };
    final int nSteps = 139;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : strikes) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D function2D = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final OptionFunctionProvider1D function1D = new AmericanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final GreekResultCollection res1D = _model.getGreeks(new TrigeorgisLatticeSpecification(), function1D, SPOT, vol, interest, dividend);
                final double[] ref = new double[] {res1D.get(Greek.FAIR_PRICE), res1D.get(Greek.DELTA), res1D.get(Greek.GAMMA), res1D.get(Greek.THETA) };
                final double[] res = _model.getGreeks(function2D, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double[] resMod = new double[] {res[0], res[1], res[4], res[3] };
                assertGreeks(resMod, ref, 1.e-2);
              }
            }
          }
        }
      }
    }
  }

  /**
   * s1 \equiv s2
   */
  @Test
  public void greeksSameSpotsTest() {
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {0.4 };
    final int nSteps = 89;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double strike : STRIKES) {
          for (final double vol : VOLS) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider2D functionAmerican = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final OptionFunctionProvider2D functionEuropean = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double[] resAmerican = _model.getGreeks(functionAmerican, SPOT, SPOT, vol, sigma2, rho, interest, dividend, dividend);
                final double[] resEuropean = _model.getGreeks(functionEuropean, SPOT, SPOT, vol, sigma2, rho, interest, dividend, dividend);
                assertGreeks(resAmerican, resEuropean, 0.2);
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
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new AmericanSpreadOptionFunctionProvider(100., 1., 53, true);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new AmericanSpreadOptionFunctionProvider(100., 1., 53, true),
        new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true), null };
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
      final double error = Math.max(Math.abs(ref[i]), 1.) * eps;
      assertEquals(res[i], ref[i], error);
    }
  }
}
