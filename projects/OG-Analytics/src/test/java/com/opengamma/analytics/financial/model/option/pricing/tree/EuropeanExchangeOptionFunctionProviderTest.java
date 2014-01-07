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

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanExchangeOptionFunctionProviderTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTri = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.017, 0.05 };
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
    final int nSteps = 91;
    final int nStepsTri = 81;
    final double quant2 = 2.;
    final double[] quant1Set = new double[] {1., 2., 3. };

    final double div2 = 0.01;

    for (final double interest : INTERESTS) {
      for (final double vol : VOLS) {
        for (final double spot2 : spotSet2) {
          for (final double rho : rhoSet) {
            for (final double dividend : DIVIDENDS) {
              for (final double quant1 : quant1Set) {
                final OptionFunctionProvider2D function = new EuropeanExchangeOptionFunctionProvider(TIME, nSteps, quant1, quant2);
                double exactDiv = price(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double refDiv = Math.max(exactDiv, 1.) * 1.e-2;
                assertEquals(resDiv, exactDiv, refDiv);

                final OptionFunctionProvider2D functionTri = new EuropeanExchangeOptionFunctionProvider(TIME, nStepsTri, quant1, quant2);
                final double resDivTri = _modelTri.getPrice(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                assertEquals(resDivTri, exactDiv, refDiv);
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
    final int nSteps = 201;
    final int nStepsTri = 31;//181 for 1.e-2
    final double quant2 = 2.;
    final double[] quant1Set = new double[] {1., 2., 3. };

    final double div2 = 0.01;

    for (final double interest : INTERESTS) {
      for (final double vol : VOLS) {
        for (final double spot2 : spotSet2) {
          for (final double rho : rhoSet) {
            for (final double dividend : DIVIDENDS) {
              for (final double quant1 : quant1Set) {
                final OptionFunctionProvider2D function = new EuropeanExchangeOptionFunctionProvider(TIME, nSteps, quant1, quant2);
                double price = price(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double delta1 = delta1(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double delta2 = delta2(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double theta = theta(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double gamma1 = gamma1(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double gamma2 = gamma2(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                double cross = crossGamma(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
                final double[] exact = new double[] {price, delta1, delta2, theta, gamma1, gamma2, cross };
                final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                assertGreeks(res, exact, 1.e-2);

                final OptionFunctionProvider2D functionTri = new EuropeanExchangeOptionFunctionProvider(TIME, nStepsTri, quant1, quant2);
                final double[] resTri = _modelTri.getGreeks(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                assertGreeks(resTri, exact, 1.e-1);
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
  public void getQuantityTest() {
    final OptionFunctionProvider2D function = new EuropeanExchangeOptionFunctionProvider(TIME, 1001, 3., 2.);
    assertEquals(((EuropeanExchangeOptionFunctionProvider) function).getQuantity1(), 3.);
    assertEquals(((EuropeanExchangeOptionFunctionProvider) function).getQuantity2(), 2.);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getSignTest() {
    final OptionFunctionProvider2D function = new EuropeanExchangeOptionFunctionProvider(TIME, 1001, 3., 2.);
    function.getSign();
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getStrikeTest() {
    final OptionFunctionProvider2D function = new EuropeanExchangeOptionFunctionProvider(TIME, 1001, 3., 2.);
    function.getStrike();
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void minusQuant1Test() {
    new EuropeanExchangeOptionFunctionProvider(TIME, 1001, -3., 2.);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void minusQuant2Test() {
    new EuropeanExchangeOptionFunctionProvider(TIME, 1001, 3., -2.);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new EuropeanExchangeOptionFunctionProvider(1., 53, 3., 2.);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new EuropeanExchangeOptionFunctionProvider(1., 53, 3., 2.),
        new EuropeanExchangeOptionFunctionProvider(1., 53, 4., 2.), new EuropeanExchangeOptionFunctionProvider(1., 53, 3., 1.),
        new AmericanSpreadOptionFunctionProvider(0., 1., 53, true), null };
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

  private double price(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return qs1 * Math.exp((cost1 - interest) * time) * NORMAL.getCDF(d1) - qs2 * Math.exp((cost2 - interest) * time) * NORMAL.getCDF(d2);
  }

  private double delta1(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;

    return quant1 * Math.exp((cost1 - interest) * time) * NORMAL.getCDF(d1);
  }

  private double gamma1(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;

    return quant1 * Math.exp((cost1 - interest) * time) * NORMAL.getPDF(d1) / spot1 / sigmaRootT;
  }

  private double delta2(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return -quant2 * Math.exp((cost2 - interest) * time) * NORMAL.getCDF(d2);
  }

  private double gamma2(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return quant2 * Math.exp((cost2 - interest) * time) * NORMAL.getPDF(d2) / spot2 / sigmaRootT;
  }

  private double crossGamma(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;

    return -quant1 * Math.exp((cost1 - interest) * time) * NORMAL.getPDF(d1) / spot2 / sigmaRootT;
  }

  private double theta(final double spot1, final double spot2, final double time, final double vol1, final double vol2, final double corr, final double interest, final double cost1,
      final double cost2, final double quant1, final double quant2) {
    final double qs1 = quant1 * spot1;
    final double qs2 = quant2 * spot2;
    final double volSq = vol1 * vol1 + vol2 * vol2 - 2. * corr * vol1 * vol2;
    final double sigmaRootT = Math.sqrt(volSq * time);
    final double d1 = (Math.log(qs1 / qs2) + (cost1 - cost2 + 0.5 * volSq) * time) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    final double first = (cost1 - interest) * qs1 * Math.exp((cost1 - interest) * time) * NORMAL.getCDF(d1) - (cost2 - interest) * qs2 * Math.exp((cost2 - interest) * time) * NORMAL.getCDF(d2);
    final double second = qs1 * Math.exp((cost1 - interest) * time) * NORMAL.getPDF(d1) * Math.sqrt(volSq) * 0.5 / Math.sqrt(time);
    return -first - second;
  }

  //  /**
  //   * 
  //   */
  //  @Test
  //  public void functionTest() {
  //    final double eps = 1.e-6;
  //    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
  //    final double sigma2 = 0.15;
  //    final double[] rhoSet = new double[] {-0.1, 0.6 };
  //    final double quant2 = 2.;
  //    final double[] quant1Set = new double[] {1., 2., 3. };
  //
  //    final double div2 = 0.01;
  //
  //    for (final double interest : INTERESTS) {
  //      for (final double vol : VOLS) {
  //        for (final double spot2 : spotSet2) {
  //          for (final double rho : rhoSet) {
  //            for (final double dividend : DIVIDENDS) {
  //              for (final double quant1 : quant1Set) {
  //                final double delta1 = delta1(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double gamma1 = gamma1(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double theta = theta(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double delta2 = delta2(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double gamma2 = gamma2(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double crossGamma = crossGamma(SPOT, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //
  //                final double upSpot1 = price(SPOT + eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downSpot1 = price(SPOT - eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double upSpot2 = price(SPOT, spot2 + eps, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downSpot2 = price(SPOT, spot2 - eps, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //
  //                final double upSpotDelta1 = delta1(SPOT + eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downSpotDelta1 = delta1(SPOT - eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double upSpotDelta2 = delta2(SPOT, spot2 + eps, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downSpotDelta2 = delta2(SPOT, spot2 - eps, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double upSpotDeltaForCross = delta2(SPOT + eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downSpotDeltaForCross = delta2(SPOT - eps, spot2, TIME, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //
  //                final double upTime = price(SPOT, spot2, TIME + eps, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //                final double downTime = price(SPOT, spot2, TIME - eps, vol, sigma2, rho, interest, interest - dividend, interest - div2, quant1, quant2);
  //
  //                assertEquals(delta1, 0.5 * (upSpot1 - downSpot1) / eps, eps);
  //                assertEquals(gamma1, 0.5 * (upSpotDelta1 - downSpotDelta1) / eps, eps);
  //                assertEquals(delta2, 0.5 * (upSpot2 - downSpot2) / eps, eps);
  //                assertEquals(gamma2, 0.5 * (upSpotDelta2 - downSpotDelta2) / eps, eps);
  //                assertEquals(crossGamma, 0.5 * (upSpotDeltaForCross - downSpotDeltaForCross) / eps, eps);
  //                assertEquals(theta, -0.5 * (upTime - downTime) / eps, eps);
  //              }
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }
}
