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

import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AmericanExchangeOptionFunctionProviderTest {
  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTri = new TrinomialTreeOptionPricingModel();
  private static final BjerksundStenslandModel _bs = new BjerksundStenslandModel();
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
    final int nSteps = 51;
    final int nStepsTri = 21;
    final double quant2 = 2.;
    final double[] quant1Set = new double[] {1., 2., 3. };

    final double div2 = 0.01;

    for (final double interest : INTERESTS) {
      for (final double vol : VOLS) {
        for (final double spot2 : spotSet2) {
          for (final double rho : rhoSet) {
            for (final double dividend : DIVIDENDS) {
              for (final double quant1 : quant1Set) {
                final OptionFunctionProvider2D function = new AmericanExchangeOptionFunctionProvider(TIME, nSteps, quant1, quant2);
                final double volhat = Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2);
                final double b1 = interest - dividend;
                final double b2 = interest - div2;
                final double appDiv = _bs.price(SPOT * quant1, quant2 * spot2, interest - b2, b1 - b2, TIME, volhat, true);
                final double resDiv = _model.getPrice(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double refDiv = Math.max(appDiv, 0.1) * 1.e-1;
                assertEquals(resDiv, appDiv, refDiv);

                final OptionFunctionProvider2D functionTri = new AmericanExchangeOptionFunctionProvider(TIME, nStepsTri, quant1, quant2);
                final double resDivTri = _modelTri.getPrice(functionTri, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                assertEquals(resDivTri, resDiv, Math.max(resDiv, 0.1) * 1.e-1);

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

  /**
   *
   */
  @Test
  public void greeksTest() {
    final double eps = 1.e-6;

    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double sigma2 = 0.15;
    final double[] rhoSet = new double[] {-0.1, 0.6 };
    final int nSteps = 91;
    final double quant2 = 2.;
    final double[] quant1Set = new double[] {1., 2., 3. };

    final double div2 = 0.01;

    for (final double interest : INTERESTS) {
      for (final double vol : VOLS) {
        for (final double spot2 : spotSet2) {
          for (final double rho : rhoSet) {
            for (final double dividend : DIVIDENDS) {
              for (final double quant1 : quant1Set) {
                final OptionFunctionProvider2D function = new AmericanExchangeOptionFunctionProvider(TIME, nSteps, quant1, quant2);
                final double volhat = Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2);
                final double b1 = interest - dividend;
                final double b2 = interest - div2;
                final double[] app = _bs.getPriceAdjoint(SPOT * quant1, quant2 * spot2, interest - b2, b1 - b2, TIME, volhat, true);
                final double price = app[0];
                final double delta1 = quant1 * app[1];
                final double delta2 = quant2 * app[2];
                //                final double theta = app[5];
                final double[] appSpot1Up = _bs.getPriceAdjoint((SPOT + eps) * quant1, quant2 * spot2, interest - b2, b1 - b2, TIME, volhat, true);
                final double[] appSpot1Down = _bs.getPriceAdjoint((SPOT - eps) * quant1, quant2 * spot2, interest - b2, b1 - b2, TIME, volhat, true);
                final double[] appSpot2Up = _bs.getPriceAdjoint(SPOT * quant1, quant2 * (spot2 + eps), interest - b2, b1 - b2, TIME, volhat, true);
                final double[] appSpot2Down = _bs.getPriceAdjoint(SPOT * quant1, quant2 * (spot2 - eps), interest - b2, b1 - b2, TIME, volhat, true);

                final double gamma1 = quant1 * 0.5 * (appSpot1Up[1] - appSpot1Down[1]) / eps;
                final double gamma2 = quant2 * 0.5 * (appSpot2Up[2] - appSpot2Down[2]) / eps;
                final double cross = quant2 * 0.5 * (appSpot1Up[2] - appSpot1Down[2]) / eps;
                //                final double[] exact = new double[] {price, delta1, delta2, -theta, gamma1, gamma2, cross };
                final double[] res = _model.getGreeks(function, SPOT, spot2, vol, sigma2, rho, interest, dividend, div2);
                final double[] exactMod = new double[] {price, delta1, delta2, gamma1, gamma2, cross };
                final double[] resMod = new double[] {res[0], res[1], res[2], res[4], res[5], res[6] };
                assertGreeks(resMod, exactMod, 1.e-1);
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
    final OptionFunctionProvider2D function = new AmericanExchangeOptionFunctionProvider(2., 1001, 3., 2.);
    assertEquals(((AmericanExchangeOptionFunctionProvider) function).getQuantity1(), 3.);
    assertEquals(((AmericanExchangeOptionFunctionProvider) function).getQuantity2(), 2.);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getSignTest() {
    final OptionFunctionProvider2D function = new AmericanExchangeOptionFunctionProvider(2., 1001, 3., 2.);
    function.getSign();
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getStrikeTest() {
    final OptionFunctionProvider2D function = new AmericanExchangeOptionFunctionProvider(2., 1001, 3., 2.);
    function.getStrike();
  }

  /**
   *
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void minusQuant1Test() {
    new AmericanExchangeOptionFunctionProvider(2., 1001, -3., 2.);
  }

  /**
   *
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void minusQuant2Test() {
    new AmericanExchangeOptionFunctionProvider(2., 1001, 3., -2.);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider2D ref = new AmericanExchangeOptionFunctionProvider(1., 53, 3., 2.);
    final OptionFunctionProvider2D[] function = new OptionFunctionProvider2D[] {ref, new AmericanExchangeOptionFunctionProvider(1., 53, 3., 2.),
        new AmericanExchangeOptionFunctionProvider(1., 53, 4., 2.), new AmericanExchangeOptionFunctionProvider(1., 53, 3., 1.),
        new EuropeanExchangeOptionFunctionProvider(1., 53, 3., 2.), null };
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
