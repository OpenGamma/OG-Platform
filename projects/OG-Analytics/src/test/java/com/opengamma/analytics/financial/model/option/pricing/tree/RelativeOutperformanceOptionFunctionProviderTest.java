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

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RelativeOutperformanceOptionFunctionProviderTest {

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTri = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {0.9, 1., 1.1 };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {0.017, 0.05 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.014 };

  /**
   * 
   */
  @Test
  public void priceTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 95;
    final int nStepsTri = 66;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double strike : STRIKES) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider2D function = new RelativeOutperformanceOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double rhoVols = rho * vol * sigma2;
                  final double fValue = SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / spot2;
                  final double exactDiv = Math.exp(-interest * TIME) * BlackFormulaRepository.price(fValue, strike, TIME,
                      Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rhoVols), isCall);
                  final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  final double refDiv = Math.max(exactDiv, 1.) * 1.e-3;
                  assertEquals(resDiv, exactDiv, refDiv);

                  final OptionFunctionProvider2D functionTri = new RelativeOutperformanceOptionFunctionProvider(strike, TIME, nStepsTri, isCall);
                  final double resDivTri = _modelTri.getPrice(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertEquals(resDivTri, exactDiv, refDiv);
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
  public void greeksTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 95;
    final int nStepsTri = 66;
    final double div2 = 0.01;
    final double eps = 1.e-6;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                for (final double strike : STRIKES) {
                  final OptionFunctionProvider2D function = new RelativeOutperformanceOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double rhoVols = rho * vol * sigma2;
                  final double volhat = Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rhoVols);
                  final double fValue = SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / spot2;

                  final double price = Math.exp(-interest * TIME) * BlackFormulaRepository.price(fValue, strike, TIME, volhat, isCall);
                  final double delta1 = Math.exp(-interest * TIME) * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) *
                      BlackFormulaRepository.delta(fValue, strike, TIME, volhat, isCall) / spot2;
                  final double delta2 = -Math.exp(-interest * TIME) * SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / spot2 / spot2 *
                      BlackFormulaRepository.delta(fValue, strike, TIME, volhat, isCall);

                  final double upForTheta = Math.exp(-interest * (TIME + eps)) *
                      BlackFormulaRepository.price(SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * (TIME + eps)) / spot2, strike, (TIME + eps), volhat, isCall);
                  final double downForTheta = Math.exp(-interest * (TIME - eps)) *
                      BlackFormulaRepository.price(SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * (TIME - eps)) / spot2, strike, (TIME - eps), volhat, isCall);
                  final double theta = -0.5 * (upForTheta - downForTheta) / eps;

                  final double gamma1 = Math.exp(-interest * TIME) * Math.exp(2. * (div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) *
                      BlackFormulaRepository.gamma(fValue, strike, TIME, volhat) / spot2 / spot2;
                  final double gamma2 = Math.exp(-interest * TIME) * Math.exp(2. * (div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) * SPOT * SPOT *
                      BlackFormulaRepository.gamma(fValue, strike, TIME, volhat) / spot2 / spot2 / spot2 / spot2 + 2. * Math.exp(-interest * TIME) * SPOT *
                      Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / spot2 / spot2 / spot2 *
                      BlackFormulaRepository.delta(fValue, strike, TIME, volhat, isCall);

                  final double upForCross = Math.exp(-interest * TIME) * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) *
                      BlackFormulaRepository.delta(SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / (spot2 + eps), strike, TIME, volhat, isCall) / (spot2 + eps);
                  final double downForCross = Math.exp(-interest * TIME) * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) *
                      BlackFormulaRepository.delta(SPOT * Math.exp((div2 - dividend + sigma2 * sigma2 - rhoVols) * TIME) / (spot2 - eps), strike, TIME, volhat, isCall) / (spot2 - eps);
                  final double cross = 0.5 * (upForCross - downForCross) / eps;

                  final double[] ref = new double[] {price, delta1, delta2, theta, gamma1, gamma2, cross };
                  final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertGreeks(res, ref, 1.e-3);

                  final OptionFunctionProvider2D functionTri = new RelativeOutperformanceOptionFunctionProvider(strike, TIME, nStepsTri, isCall);
                  final double[] resTri = _modelTri.getGreeks(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertGreeks(resTri, ref, 1.e-3);
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
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new RelativeOutperformanceOptionFunctionProvider(100., 1., 53, true);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new RelativeOutperformanceOptionFunctionProvider(100., 1., 53, true),
        new AmericanSpreadOptionFunctionProvider(100., 1., 53, true), null };
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
