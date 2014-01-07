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

import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoAssetCorrelationOptionFunctionProviderTest {
  private static final BivariateNormalDistribution _bivariate = new BivariateNormalDistribution();
  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTri = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES1 = new double[] {95., 105., 115. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {0.017, 0.05 };
  private static final double[] VOLS = new double[] {0.05, 0.25 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.014 };

  /**
   * 
   */
  @Test
  public void priceTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final double strike2 = 104.;
    final int nSteps = 69;
    final int nStepsTri = 63;

    final double div2 = 0.01;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      //    final boolean isCall = false;
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double strike1 : STRIKES1) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider2D function = new TwoAssetCorrelationOptionFunctionProvider(strike1, strike2, TIME, nSteps, isCall);
                  double exactDiv = price(SPOT, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  final double refDiv = Math.max(exactDiv, 1.) * 0.2;
                  assertEquals(resDiv, exactDiv, refDiv);

                  final OptionFunctionProvider2D functionTri = new TwoAssetCorrelationOptionFunctionProvider(strike1, strike2, TIME, nStepsTri, isCall);
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
    final int nSteps = 199;
    final int nStepsTri = 13;
    final double div2 = 0.01;
    final double strike2 = 104.;
    final double eps = 1.e-6;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double spot2 : spotSet2) {
            for (final double rho : rhoSet) {
              for (final double dividend : DIVIDENDS) {
                for (final double strike1 : STRIKES1) {
                  final OptionFunctionProvider2D function = new TwoAssetCorrelationOptionFunctionProvider(strike1, strike2, TIME, nSteps, isCall);

                  final double price = price(SPOT, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double delta1 = delta1(SPOT, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double delta2 = delta2(SPOT, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double theta = -theta(SPOT, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);

                  final double upForGamma1 = delta1(SPOT + eps, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double downForGamma1 = delta1(SPOT - eps, spot2, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double gamma1 = 0.5 * (upForGamma1 - downForGamma1) / eps;

                  final double upForGamma2 = delta2(SPOT, spot2 + eps, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double downForGamma2 = delta2(SPOT, spot2 - eps, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double gamma2 = 0.5 * (upForGamma2 - downForGamma2) / eps;

                  final double upForCross = delta1(SPOT, spot2 + eps, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double downForCross = delta1(SPOT, spot2 - eps, strike1, strike2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, isCall);
                  final double cross = 0.5 * (upForCross - downForCross) / eps;

                  final double[] ref = new double[] {price, delta1, delta2, theta, gamma1, gamma2, cross };
                  final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  assertGreeks(res, ref, 1.e-1);

                  final OptionFunctionProvider2D functionTri = new TwoAssetCorrelationOptionFunctionProvider(strike1, strike2, TIME, nStepsTri, isCall);
                  final double[] resTri = _modelTri.getGreeks(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                  /*
                   * asset1 gamma is poorly approximated for cetain data sets
                   */
                  assertGreeks(resTri, ref, 1.);
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
  public void getStrike1Test() {
    final TwoAssetCorrelationOptionFunctionProvider function = new TwoAssetCorrelationOptionFunctionProvider(100., 101., 1., 1001, true);
    assertEquals(function.getStrike1(), 100.);
  }

  /**
   * 
   */
  @Test
  public void getStrike2Test() {
    final TwoAssetCorrelationOptionFunctionProvider function = new TwoAssetCorrelationOptionFunctionProvider(100., 101., 1., 1001, true);
    assertEquals(function.getStrike2(), 101.);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getStrikeFailTest() {
    final TwoAssetCorrelationOptionFunctionProvider function = new TwoAssetCorrelationOptionFunctionProvider(100., 101., 1., 1001, true);
    function.getStrike();
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrike2Test() {
    new TwoAssetCorrelationOptionFunctionProvider(100., -101., 1., 1001, true);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new TwoAssetCorrelationOptionFunctionProvider(100., 90., 1., 53, true);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new TwoAssetCorrelationOptionFunctionProvider(100., 90., 1., 53, true),
        new TwoAssetCorrelationOptionFunctionProvider(100., 92., 1., 53, true), new AmericanSpreadOptionFunctionProvider(100., 1., 53, true), null };
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

  private double price(final double spot1, final double spot2, final double strike1, final double strike2, final double time, final double vol1, final double vol2, final double cor,
      final double interest, final double cost1, final double cost2, final boolean isCall) {
    final double rootT = Math.sqrt(time);
    final double sigmaRootT1 = vol1 * rootT;
    final double sigmaRootT2 = vol2 * rootT;
    final double y1 = (Math.log(spot1 / strike1) + (cost1 - 0.5 * vol1 * vol1) * time) / sigmaRootT1;
    final double y2 = (Math.log(spot2 / strike2) + (cost2 - 0.5 * vol2 * vol2) * time) / sigmaRootT2;
    final double sign = isCall ? 1. : -1.;

    final double res = sign *
        (_bivariate.getCDF(new double[] {sign * (y2 + sigmaRootT2), sign * (y1 + cor * sigmaRootT2), cor }) * spot2 * Math.exp((cost2 - interest) * time) - strike2 * Math.exp(-interest * time) *
            _bivariate.getCDF(new double[] {sign * y2, sign * y1, cor }));
    return res;
  }

  private double delta1(final double spot1, final double spot2, final double strike1, final double strike2, final double time, final double vol1, final double vol2, final double cor,
      final double interest, final double cost1, final double cost2, final boolean isCall) {
    final double eps = 1.e-6;
    final double priceSpotUp = price(spot1 + eps, spot2, strike1, strike2, time, vol1, vol2, cor, interest, cost1, cost2, isCall);
    final double priceSpotDown = price(spot1 - eps, spot2, strike1, strike2, time, vol1, vol2, cor, interest, cost1, cost2, isCall);
    return 0.5 * (priceSpotUp - priceSpotDown) / eps;
  }

  private double delta2(final double spot1, final double spot2, final double strike1, final double strike2, final double time, final double vol1, final double vol2, final double cor,
      final double interest, final double cost1, final double cost2, final boolean isCall) {
    final double eps = 1.e-6;
    final double priceSpotUp = price(spot1, spot2 + eps, strike1, strike2, time, vol1, vol2, cor, interest, cost1, cost2, isCall);
    final double priceSpotDown = price(spot1, spot2 - eps, strike1, strike2, time, vol1, vol2, cor, interest, cost1, cost2, isCall);
    return 0.5 * (priceSpotUp - priceSpotDown) / eps;
  }

  private double theta(final double spot1, final double spot2, final double strike1, final double strike2, final double time, final double vol1, final double vol2, final double cor,
      final double interest, final double cost1, final double cost2, final boolean isCall) {
    final double eps = 1.e-6;
    final double priceTimeUp = price(spot1, spot2, strike1, strike2, time + eps, vol1, vol2, cor, interest, cost1, cost2, isCall);
    final double priceTimeDown = price(spot1, spot2, strike1, strike2, time - eps, vol1, vol2, cor, interest, cost1, cost2, isCall);
    return 0.5 * (priceTimeUp - priceTimeDown) / eps;
  }

}
