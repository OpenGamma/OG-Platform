/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRJohnsonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("unchecked")
public class SABRExtrapolationLeftFunctionTest {
  private static final double EPS = 1.0e-6;

  private static final double FORWARD = 0.13;
  private static final double CUTOFF = 0.08;
  private static final double EXPIRY = 1.5;
  private static final double MU = 1.1;

  private static final double NU = 0.8;
  private static final double RHO = -0.65;
  private static final double BETA = 0.76;
  private static final double ALPHA = 1.6;
  private static final SABRFormulaData DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);

  private static final SABRHaganVolatilityFunction FUNC_HAGAN = new SABRHaganVolatilityFunction();
  private static final SABRJohnsonVolatilityFunction FUNC_JOHNSON = new SABRJohnsonVolatilityFunction();
  private static final SABRHaganAlternativeVolatilityFunction FUNC_HAGAN_ALT = new SABRHaganAlternativeVolatilityFunction();
  private static final SABRBerestyckiVolatilityFunction FUNC_BERESTYCKI = new SABRBerestyckiVolatilityFunction();
  private static final SABRPaulotVolatilityFunction FUNC_PAULOT = new SABRPaulotVolatilityFunction();
  private static final VolatilityFunctionProvider<SABRFormulaData>[] FUNCTIONS = new VolatilityFunctionProvider[] {FUNC_HAGAN, FUNC_JOHNSON, FUNC_HAGAN_ALT, FUNC_BERESTYCKI, FUNC_PAULOT };

  /**
   * C2 continuity and accessors are tested
   */
  @Test
  public void smoothnessAndAccessorTest() {
    for (VolatilityFunctionProvider<SABRFormulaData> func : FUNCTIONS) {
      SABRExtrapolationLeftFunction left = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY, MU, func);
      for (boolean isCall : new boolean[] {true, false }) {
        EuropeanVanillaOption optionBase = new EuropeanVanillaOption(CUTOFF, EXPIRY, isCall);
        EuropeanVanillaOption optionUp = new EuropeanVanillaOption(CUTOFF + EPS, EXPIRY, isCall);
        EuropeanVanillaOption optionDw = new EuropeanVanillaOption(CUTOFF - EPS, EXPIRY, isCall);
        double priceBase = left.price(optionBase);
        double priceUp = left.price(optionUp);
        double priceDw = left.price(optionDw);
        assertEquals(priceBase, priceUp, EPS);
        assertEquals(priceBase, priceDw, EPS);
        EuropeanVanillaOption optionUpUp = new EuropeanVanillaOption(CUTOFF + 2.0 * EPS, EXPIRY, isCall);
        EuropeanVanillaOption optionDwDw = new EuropeanVanillaOption(CUTOFF - 2.0 * EPS, EXPIRY, isCall);
        double priceUpUp = left.price(optionUpUp);
        double priceDwDw = left.price(optionDwDw);
        double firstUp = (-0.5 * priceUpUp + 2.0 * priceUp - 1.5 * priceBase) / EPS;
        double firstDw = (-2.0 * priceDw + 0.5 * priceDwDw + 1.5 * priceBase) / EPS;
        assertEquals(firstDw, firstUp, EPS);

        // The second derivative values are poorly connected due to finite difference approximation 
        double firstUpUp = 0.5 * (priceUpUp - priceBase) / EPS;
        double firstDwDw = 0.5 * (priceBase - priceDwDw) / EPS;
        double secondUp = (firstUpUp - firstUp) / EPS;
        double secondDw = (firstDw - firstDwDw) / EPS;
        double secondRef = 0.5 * (firstUpUp - firstDwDw) / EPS;
        assertEquals(secondRef, secondUp, secondRef * 0.15);
        assertEquals(secondRef, secondDw, secondRef * 0.15);
      }

      assertTrue(left.getSABRFunction().equals(func));
      assertEquals(EXPIRY, left.getTimeToExpiry());
      assertEquals(MU, left.getMu());
      assertEquals(CUTOFF, left.getCutOffStrike());
      assertEquals(DATA, left.getSabrData());

    }

  }

  /**
   * Due to large numerical error with this setup, only C0 is checked.
   * Also if Hagan formula (or its kind) is used, negative vols are returned and they are modified as 0.0 internally.
   */
  @Test
  public void smallCutoffTest() {
    double smallCutoff = 0.5e-6;
    for (VolatilityFunctionProvider<SABRFormulaData> func : FUNCTIONS) {
      SABRExtrapolationLeftFunction left = new SABRExtrapolationLeftFunction(FORWARD * 0.01, DATA, smallCutoff, EXPIRY, MU, func);
      EuropeanVanillaOption optionBase = new EuropeanVanillaOption(smallCutoff, EXPIRY, false);
      EuropeanVanillaOption optionUp = new EuropeanVanillaOption(smallCutoff + EPS * 0.1, EXPIRY, false);
      EuropeanVanillaOption optionDw = new EuropeanVanillaOption(smallCutoff - EPS * 0.1, EXPIRY, false);
      double priceBase = left.price(optionBase);
      double priceUp = left.price(optionUp);
      double priceDw = left.price(optionDw);
      assertEquals(priceBase, priceUp, EPS);
      assertEquals(priceBase, priceDw, EPS);

      if (priceBase == 0.0) {
        assertEquals(left.getParameter()[0], -100.0, 1.e-12);
        assertEquals(left.getParameter()[1], 0.0, 1.e-12);
        assertEquals(left.getParameter()[2], 0.0, 1.e-12);
      }
    }
  }

  /**
   * Due to large numerical error with this setup, only C0 is checked.
   * Also if Hagan formula (or its kind) is used, negative vols are returned and they are modified as 0.0 internally.
   */
  @Test
  public void smallForwardTest() {
    double smallForward = 0.9e-6;
    double smallCutoff = 0.5e-6;
    for (VolatilityFunctionProvider<SABRFormulaData> func : FUNCTIONS) {
      SABRExtrapolationLeftFunction left = new SABRExtrapolationLeftFunction(smallForward, DATA, smallCutoff, EXPIRY, MU, func);
      EuropeanVanillaOption optionBase = new EuropeanVanillaOption(smallCutoff, EXPIRY, false);
      EuropeanVanillaOption optionUp = new EuropeanVanillaOption(smallCutoff + EPS * 0.1, EXPIRY, false);
      EuropeanVanillaOption optionDw = new EuropeanVanillaOption(smallCutoff - EPS * 0.1, EXPIRY, false);
      double priceBase = left.price(optionBase);
      double priceUp = left.price(optionUp);
      double priceDw = left.price(optionDw);
      assertEquals(priceBase, priceUp, EPS * 10.0);
      assertEquals(priceBase, priceDw, EPS * 10.0);
    }
  }

  /**
   * Extrapolator is not calibrated in this case, then the gap is produced at the cutoff. 
   */
  @Test
  public void smallExpiryTest() {
    double smallExpiry = 0.5e-6;

    for (VolatilityFunctionProvider<SABRFormulaData> func : FUNCTIONS) {
      SABRExtrapolationLeftFunction left = new SABRExtrapolationLeftFunction(FORWARD * 0.01, DATA, CUTOFF, smallExpiry, MU, func);
      EuropeanVanillaOption optionBase = new EuropeanVanillaOption(CUTOFF, smallExpiry, false);
      EuropeanVanillaOption optionUp = new EuropeanVanillaOption(CUTOFF + EPS * 0.1, smallExpiry, false);
      EuropeanVanillaOption optionDw = new EuropeanVanillaOption(CUTOFF - EPS * 0.1, smallExpiry, false);
      double priceBase = left.price(optionBase);
      double priceUp = left.price(optionUp);
      double priceDw = left.price(optionDw);
      assertEquals(priceBase, priceUp, EPS);
      assertEquals(0.0, priceDw, EPS);

      if (priceBase == 0.0) {
        assertEquals(left.getParameter()[0], -1.0E4, 1.e-12);
        assertEquals(left.getParameter()[1], 0.0, 1.e-12);
        assertEquals(left.getParameter()[2], 0.0, 1.e-12);
      }
    }
  }

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsTest() {
    SABRExtrapolationLeftFunction func1 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func2 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY, MU * 0.9, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func3 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY * 0.9, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func4 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF * 0.9, EXPIRY, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func5 = new SABRExtrapolationLeftFunction(FORWARD * 0.9, DATA, CUTOFF, EXPIRY, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func6 = new SABRExtrapolationLeftFunction(FORWARD, DATA.withAlpha(ALPHA * 0.97), CUTOFF, EXPIRY, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func7 = func1;
    SABRExtrapolationLeftFunction func8 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY, MU, FUNCTIONS[0]);
    SABRExtrapolationLeftFunction func9 = new SABRExtrapolationLeftFunction(FORWARD, DATA, CUTOFF, EXPIRY, MU, FUNCTIONS[1]);

    assertTrue(func1.equals(func1));

    assertFalse(func1.equals(func2));
    assertFalse(func2.equals(func1));
    assertFalse(func1.hashCode() == func2.hashCode());

    assertFalse(func1.hashCode() == func3.hashCode());
    assertFalse(func3.equals(func1));
    assertFalse(func1.equals(func3));

    assertFalse(func1.hashCode() == func4.hashCode());
    assertFalse(func4.equals(func1));
    assertFalse(func1.equals(func4));

    assertFalse(func1.hashCode() == func5.hashCode());
    assertFalse(func5.equals(func1));
    assertFalse(func1.equals(func5));

    assertFalse(func1.hashCode() == func6.hashCode());
    assertFalse(func6.equals(func1));
    assertFalse(func1.equals(func6));

    assertTrue(func7.equals(func1));
    assertTrue(func1.equals(func7));
    assertTrue(func1.hashCode() == func7.hashCode());

    assertTrue(func8.equals(func1));
    assertTrue(func1.equals(func8));
    assertTrue(func8.hashCode() == func8.hashCode());

    assertFalse(func1.hashCode() == func9.hashCode());
    assertFalse(func9.equals(func1));
    assertFalse(func1.equals(func9));

    assertFalse(func1.equals(null));
    assertFalse(func1.equals(new double[] {1.2 }));
  }

}
