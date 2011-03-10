package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

public class FourierPricerTest {
  private static final BlackFunctionData DATA = new BlackFunctionData(100, 0.95, 0.4);
  private static final EuropeanVanillaOption CALL = new EuropeanVanillaOption(98, 3, true);
  private static final EuropeanVanillaOption PUT = new EuropeanVanillaOption(98, 3, false);
  private static final CGMYCharacteristicExponent1 CGMY = new CGMYCharacteristicExponent1(2, 2, 2, 0.4);
  private static final GaussianCharacteristicExponent1 CE = new GaussianCharacteristicExponent1(0.03, 0.4);
  private static final BlackPriceFunction BLACK_PRICE = new BlackPriceFunction();
  private static final double ALPHA = -0.5;
  private static final double TOL = 1e-8;
  private static final FourierPricer1 PRICER = new FourierPricer1();
  private static final double EPS = 1e-6;

  @Test(expected = IllegalArgumentException.class)
  public void testNullIntegrator() {
    new FourierPricer1(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    PRICER.price(null, CALL, CGMY, ALPHA, TOL, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption() {
    PRICER.price(DATA, null, CGMY, ALPHA, TOL, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCE() {
    PRICER.price(DATA, CALL, null, ALPHA, TOL, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowAlpha() {
    PRICER.price(DATA, CALL, CGMY, -100, TOL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighAlpha() {
    PRICER.price(DATA, CALL, CGMY, 100, TOL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    PRICER.price(DATA, CALL, CGMY, ALPHA, -TOL, false);
  }

  @Test
  public void test() {
    assertEquals(PRICER.price(DATA, CALL, CE, ALPHA, TOL), PRICER.price(DATA, CALL, CE, ALPHA, TOL, false), 0);
    assertEquals(PRICER.price(DATA, CALL, CE, ALPHA, TOL), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, ALPHA, TOL), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, CALL, CE, ALPHA, TOL, true), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS * EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, ALPHA, TOL, true), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS * EPS);
    assertEquals(PRICER.price(DATA, CALL, CE, -1.5, TOL), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, -1.5, TOL), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, CALL, CE, -1.5, TOL, true), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS * EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, -1.5, TOL, true), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS * EPS);
    assertEquals(PRICER.price(DATA, CALL, CE, 1.5, TOL), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, 1.5, TOL), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS);
    assertEquals(PRICER.price(DATA, CALL, CE, 1.5, TOL, true), BLACK_PRICE.getPriceFunction(CALL).evaluate(DATA), EPS * EPS);
    assertEquals(PRICER.price(DATA, PUT, CE, 1.5, TOL, true), BLACK_PRICE.getPriceFunction(PUT).evaluate(DATA), EPS * EPS);
  }
}
