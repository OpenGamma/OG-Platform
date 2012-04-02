package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.fourier.EuropeanCallFourierTransform;
import com.opengamma.analytics.financial.model.option.pricing.fourier.GaussianMartingaleCharacteristicExponent;
import com.opengamma.analytics.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;

public class EuropeanCallFourierTransformTest {
  private static final  MartingaleCharacteristicExponent CE = new GaussianMartingaleCharacteristicExponent(0.2);
  private static final EuropeanCallFourierTransform FT = new EuropeanCallFourierTransform(CE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExponent() {
    new EuropeanCallFourierTransform(null);
  }

  @Test
  public void test() {
    assertEquals(FT.getCharacteristicExponent(), CE);
    EuropeanCallFourierTransform other = new EuropeanCallFourierTransform(CE);
    assertEquals(other, FT);
    assertEquals(other.hashCode(), FT.hashCode());
    other = new EuropeanCallFourierTransform(new GaussianMartingaleCharacteristicExponent(0.3));
    assertFalse(other.equals(FT));
  }
}
