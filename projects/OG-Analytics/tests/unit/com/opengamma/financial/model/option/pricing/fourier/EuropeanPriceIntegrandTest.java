package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

public class EuropeanPriceIntegrandTest {
  private static final GaussianCharacteristicExponent1 GAUSSIAN = new GaussianCharacteristicExponent1(0.2, 0.5);
  private static final double ALPHA = 0.4;
  private static final boolean USE_VARIANCE_REDUCTION = false;
  private static final EuropeanPriceIntegrand1 INTEGRAND = new EuropeanPriceIntegrand1(GAUSSIAN, ALPHA, USE_VARIANCE_REDUCTION);
  private static final BlackFunctionData DATA = new BlackFunctionData(100, 1, 0.4);
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(100, 4, true);

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent() {
    new EuropeanPriceIntegrand1(null, ALPHA, USE_VARIANCE_REDUCTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTEGRAND.getFunction(null, OPTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption() {
    INTEGRAND.getFunction(DATA, null);
  }

  @Test
  public void test() {
    assertEquals(INTEGRAND.getAlpha(), ALPHA, 0);
    assertEquals(INTEGRAND.getCharacteristicExponent(), new MeanCorrectedCharacteristicExponent1(GAUSSIAN));
    assertEquals(INTEGRAND.useVarianceReduction(), USE_VARIANCE_REDUCTION);
    EuropeanPriceIntegrand1 other = new EuropeanPriceIntegrand1(GAUSSIAN, ALPHA, USE_VARIANCE_REDUCTION);
    assertEquals(other, INTEGRAND);
    assertEquals(other.hashCode(), INTEGRAND.hashCode());
    other = new EuropeanPriceIntegrand1(new GaussianCharacteristicExponent1(0.1, 0.6), ALPHA, USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
    other = new EuropeanPriceIntegrand1(GAUSSIAN, ALPHA + 1, USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
    other = new EuropeanPriceIntegrand1(GAUSSIAN, ALPHA, !USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
  }
}
