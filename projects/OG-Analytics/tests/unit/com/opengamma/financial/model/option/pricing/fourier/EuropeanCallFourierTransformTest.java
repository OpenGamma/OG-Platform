package com.opengamma.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

public class EuropeanCallFourierTransformTest {
  private static final CharacteristicExponent CE = new GaussianCharacteristicExponent(0.2, 0.9);
  private static final EuropeanCallFourierTransform FT = new EuropeanCallFourierTransform(CE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExponent() {
    new EuropeanCallFourierTransform(null);
  }

  @Test
  public void test() {
    assertEquals(FT.getCharacteristicExponent(), new MeanCorrectedCharacteristicExponent(CE));
    EuropeanCallFourierTransform other = new EuropeanCallFourierTransform(CE);
    assertEquals(other, FT);
    assertEquals(other.hashCode(), FT.hashCode());
    other = new EuropeanCallFourierTransform(new GaussianCharacteristicExponent(0.2, 0.1));
    assertFalse(other.equals(FT));
  }
}
