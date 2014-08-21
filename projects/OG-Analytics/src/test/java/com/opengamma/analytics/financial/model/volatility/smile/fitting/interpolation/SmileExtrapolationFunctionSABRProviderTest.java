/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test class for {@link SmileExtrapolationFunctionSABRProvider} and its subclasses. 
 * As these classes should be used with {@link SmileInterpolatorSABRWithExtrapolation}, nontrivial tests are in {@link SmileInterpolatorSABRWithExtrapolationTest}. 
 */
@Test(groups = TestGroup.UNIT)
public class SmileExtrapolationFunctionSABRProviderTest {

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void hashCodeAndEqualsErrorBDKTest() {

    BenaimDodgsonKainthExtrapolationFunctionProvider provider1 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 3.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider2 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 1.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider3 = new BenaimDodgsonKainthExtrapolationFunctionProvider(1.0, 3.5);
    ShiftedLogNormalExtrapolationFunctionProvider provider4 = new ShiftedLogNormalExtrapolationFunctionProvider();
    BenaimDodgsonKainthExtrapolationFunctionProvider provider5 = provider1;
    BenaimDodgsonKainthExtrapolationFunctionProvider provider6 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 3.5);

    assertTrue(provider1.equals(provider1));

    assertTrue(provider1.equals(provider5));
    assertTrue(provider5.equals(provider1));
    assertTrue(provider1.hashCode() == provider5.hashCode());

    assertTrue(provider1.equals(provider6));
    assertTrue(provider6.equals(provider1));
    assertTrue(provider1.hashCode() == provider6.hashCode());

    assertFalse(provider1.hashCode() == provider2.hashCode());
    assertFalse(provider1.equals(provider2));
    assertFalse(provider2.equals(provider1));

    assertFalse(provider1.hashCode() == provider3.hashCode());
    assertFalse(provider1.equals(provider3));
    assertFalse(provider3.equals(provider1));

    assertFalse(provider1.hashCode() == provider4.hashCode());
    assertFalse(provider1.equals(provider4));
    assertFalse(provider4.equals(provider1));

    assertFalse(provider1.equals(null));

    /**
     * Exception expected
     */
    try {
      new BenaimDodgsonKainthExtrapolationFunctionProvider(-2.0, 3.5);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("muLow should be positive", e.getMessage());
    }
    try {
      new BenaimDodgsonKainthExtrapolationFunctionProvider(2.5, -10.0);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("muHigh should be positive", e.getMessage());
    }

    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9252 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }
    double muLow = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, impliedVols[0], false) / BlackFormulaRepository.price(forward, strikes[0], expiry, impliedVols[0], false);
    double muHigh = -strikes[nStrikes - 1] * BlackFormulaRepository.dualDelta(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true);
    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes, expiry, impliedVols);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow, muHigh);

    try {
      provider.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, -strikes[0], strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("cutOffStrikeLow should be positive", e.getMessage());
    }
    try {
      provider.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, strikes[0], -strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("cutOffStrikeLow < cutOffStrikeHigh should be satisfied", e.getMessage());
    }
  }

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsErrorSLNTest() {

    ShiftedLogNormalExtrapolationFunctionProvider provider1 = new ShiftedLogNormalExtrapolationFunctionProvider("Exception");
    ShiftedLogNormalExtrapolationFunctionProvider provider2 = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    ShiftedLogNormalExtrapolationFunctionProvider provider3 = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");
    BenaimDodgsonKainthExtrapolationFunctionProvider provider4 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 3.0);
    ShiftedLogNormalExtrapolationFunctionProvider provider5 = provider1;
    ShiftedLogNormalExtrapolationFunctionProvider provider6 = new ShiftedLogNormalExtrapolationFunctionProvider("Exception");

    assertTrue(provider1.equals(provider1));

    assertTrue(provider1.equals(provider5));
    assertTrue(provider5.equals(provider1));
    assertTrue(provider1.hashCode() == provider5.hashCode());

    assertTrue(provider1.equals(provider6));
    assertTrue(provider6.equals(provider1));
    assertTrue(provider1.hashCode() == provider6.hashCode());

    assertFalse(provider1.hashCode() == provider2.hashCode());
    assertFalse(provider1.equals(provider2));
    assertFalse(provider2.equals(provider1));

    assertFalse(provider1.hashCode() == provider3.hashCode());
    assertFalse(provider1.equals(provider3));
    assertFalse(provider3.equals(provider1));

    assertFalse(provider1.hashCode() == provider4.hashCode());
    assertFalse(provider1.equals(provider4));
    assertFalse(provider4.equals(provider1));

    assertFalse(provider1.equals(null));

    /**
     * Exception expected
     */
    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9252 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }
    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes, expiry, impliedVols);
    ShiftedLogNormalExtrapolationFunctionProvider providerExce = new ShiftedLogNormalExtrapolationFunctionProvider("Exception");
    ShiftedLogNormalExtrapolationFunctionProvider providerNext = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");

    try {
      providerExce.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, -strikes[0], strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("cutOffStrikeLow should be positive", e.getMessage());
    }

    try {
      providerExce.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, forward * 1.01, strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      providerExce.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, strikes[0], forward * 0.99);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      providerNext.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, forward * 1.01, strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      providerNext.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, strikes[0], forward * 0.99);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    ShiftedLogNormalExtrapolationFunctionProvider providerFail = new ShiftedLogNormalExtrapolationFunctionProvider("None");
    try {
      providerFail.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, strikes[0], strikes[nStrikes - 1]);
    } catch (final Exception e) {
      assertEquals("Unrecognized _extrapolatorFailureBehaviour. Looking for one of Exception, Quiet, or Flat", e.getMessage());
    }
  }
}
