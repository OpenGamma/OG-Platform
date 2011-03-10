package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EuropeanCallFourierTransformTest {
  private static final CharacteristicExponent1 CE = new GaussianCharacteristicExponent1(0.2, 0.9);
  private static final EuropeanCallFourierTransform FT = new EuropeanCallFourierTransform(CE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent() {
    new EuropeanCallFourierTransform(null);
  }

  @Test
  public void test() {
    assertEquals(FT.getCharacteristicExponent(), new MeanCorrectedCharacteristicExponent1(CE));
    EuropeanCallFourierTransform other = new EuropeanCallFourierTransform(CE);
    assertEquals(other, FT);
    assertEquals(other.hashCode(), FT.hashCode());
    other = new EuropeanCallFourierTransform(new GaussianCharacteristicExponent1(0.2, 0.1));
    assertFalse(other.equals(FT));
  }
}
