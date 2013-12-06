package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanPriceIntegrandTest {
  private static final MartingaleCharacteristicExponent GAUSSIAN = new GaussianMartingaleCharacteristicExponent(0.2);
  private static final double ALPHA = 0.4;
  private static final boolean USE_VARIANCE_REDUCTION = false;
  private static final EuropeanPriceIntegrand INTEGRAND = new EuropeanPriceIntegrand(GAUSSIAN, ALPHA, USE_VARIANCE_REDUCTION);
  private static final BlackFunctionData DATA = new BlackFunctionData(100, 1, 0.4);
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(100, 4, true);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExponent() {
    new EuropeanPriceIntegrand(null, ALPHA, USE_VARIANCE_REDUCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTEGRAND.getFunction(null, OPTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    INTEGRAND.getFunction(DATA, null);
  }

  @Test
  public void test() {
    assertEquals(INTEGRAND.getAlpha(), ALPHA, 0);
    assertEquals(INTEGRAND.getCharacteristicExponent(), GAUSSIAN);
    assertEquals(INTEGRAND.useVarianceReduction(), USE_VARIANCE_REDUCTION);
    EuropeanPriceIntegrand other = new EuropeanPriceIntegrand(GAUSSIAN, ALPHA, USE_VARIANCE_REDUCTION);
    assertEquals(other, INTEGRAND);
    assertEquals(other.hashCode(), INTEGRAND.hashCode());
    other = new EuropeanPriceIntegrand(new GaussianMartingaleCharacteristicExponent(0.1), ALPHA, USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
    other = new EuropeanPriceIntegrand(GAUSSIAN, ALPHA + 1, USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
    other = new EuropeanPriceIntegrand(GAUSSIAN, ALPHA, !USE_VARIANCE_REDUCTION);
    assertFalse(other.equals(INTEGRAND));
  }
}
