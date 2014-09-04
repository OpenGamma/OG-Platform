/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test class for {@link SmileExtrapolationFunctionSABRProvider} and its subclasses. 
 * As these classes should be used with {@link SmileInterpolatorSABRWithExtrapolation}, 
 * nontrivial tests are in {@link SmileInterpolatorSABRWithExtrapolationTest}. 
 */
@SuppressWarnings("unused")
@Test(groups = TestGroup.UNIT)
public class SmileExtrapolationFunctionSABRProviderTest {

  private static final BenaimDodgsonKainthExtrapolationFunctionProvider DEFAULT_BDK_PROVIDER = new BenaimDodgsonKainthExtrapolationFunctionProvider(
      1.5, 2.0);
  private static final ShiftedLogNormalExtrapolationFunctionProvider DEFAULT_SLN_PROVIDER_EXC = new ShiftedLogNormalExtrapolationFunctionProvider(
      "Exception");
  private static final ShiftedLogNormalExtrapolationFunctionProvider DEFAULT_SLN_PROVIDER_QUI = new ShiftedLogNormalExtrapolationFunctionProvider(
      "Quiet");
  private static final SABRFormulaData SAMPLE_SABR_DATA = new SABRFormulaData(0.5, 1.0, 0.65, 0.35);
  private static final double SAMPLE_FORWARD = 1.1;
  private static final double SAMPLE_EXPIRY = 1.5;

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsErrorBDKTest() {

    BenaimDodgsonKainthExtrapolationFunctionProvider provider1 = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        2.0, 3.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider2 = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        2.0, 1.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider3 = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        1.0, 3.5);
    ShiftedLogNormalExtrapolationFunctionProvider provider4 = new ShiftedLogNormalExtrapolationFunctionProvider();
    BenaimDodgsonKainthExtrapolationFunctionProvider provider5 = provider1;
    BenaimDodgsonKainthExtrapolationFunctionProvider provider6 = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        2.0, 3.5);

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
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeLeftMuBdkTest() {
    new BenaimDodgsonKainthExtrapolationFunctionProvider(-2.0, 3.5);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeRightMuBdkTest() {
    new BenaimDodgsonKainthExtrapolationFunctionProvider(2.5, -10.0);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeBdkTest() {
    DEFAULT_BDK_PROVIDER.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, -0.7 * SAMPLE_FORWARD, 1.5 * SAMPLE_FORWARD);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowerUpperStrikeBdkTest() {
    DEFAULT_BDK_PROVIDER.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, 0.7 * SAMPLE_FORWARD, -1.5 * SAMPLE_FORWARD);
  }

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsErrorSLNTest() {

    ShiftedLogNormalExtrapolationFunctionProvider provider1 = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Exception");
    ShiftedLogNormalExtrapolationFunctionProvider provider2 = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    ShiftedLogNormalExtrapolationFunctionProvider provider3 = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");
    BenaimDodgsonKainthExtrapolationFunctionProvider provider4 = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        2.0, 3.0);
    ShiftedLogNormalExtrapolationFunctionProvider provider5 = provider1;
    ShiftedLogNormalExtrapolationFunctionProvider provider6 = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Exception");

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
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeSlnTest() {
    DEFAULT_SLN_PROVIDER_EXC.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, -0.5 * SAMPLE_FORWARD, 1.5 * SAMPLE_FORWARD);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeLowerStrikeSlnTest() {
    DEFAULT_SLN_PROVIDER_EXC.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, 1.01 * SAMPLE_FORWARD, 1.5 * SAMPLE_FORWARD);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void smallUpperStrikeSlnTest() {
    DEFAULT_SLN_PROVIDER_EXC.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, 0.5 * SAMPLE_FORWARD, 0.99 * SAMPLE_FORWARD);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nonBehaviourSlmTest() {
    ShiftedLogNormalExtrapolationFunctionProvider providerFail = new ShiftedLogNormalExtrapolationFunctionProvider(
        "None");
    providerFail.getExtrapolationFunction(SAMPLE_SABR_DATA, SAMPLE_SABR_DATA,
        new SABRHaganVolatilityFunction(), SAMPLE_FORWARD, SAMPLE_EXPIRY, -0.5 * SAMPLE_FORWARD, 1.5 * SAMPLE_FORWARD);
  }
}
