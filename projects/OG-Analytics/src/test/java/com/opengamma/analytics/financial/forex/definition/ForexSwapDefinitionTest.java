/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of ForexSwapDefinition and it conversion to derivative.
 */
@Test(groups = TestGroup.UNIT)
public class ForexSwapDefinitionTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 6, 27); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexDefinition FX_NEAR_DEFINITION = new ForexDefinition(CUR_1, CUR_2, NEAR_DATE, NOMINAL_1, FX_RATE);
  private static final ForexDefinition FX_FAR_DEFINITION = new ForexDefinition(CUR_1, CUR_2, FAR_DATE, -NOMINAL_1, FX_RATE + FORWARD_POINTS);
  private static final ForexSwapDefinition FX_SWAP_DEFINITION_LEG = new ForexSwapDefinition(FX_NEAR_DEFINITION, FX_FAR_DEFINITION);
  private static final ForexSwapDefinition FX_SWAP_DEFINITION_FIN = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNearLeg() {
    new ForexSwapDefinition(null, FX_FAR_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFarLeg() {
    new ForexSwapDefinition(FX_NEAR_DEFINITION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    new ForexSwapDefinition(null, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    new ForexSwapDefinition(CUR_1, null, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNearDate() {
    new ForexSwapDefinition(CUR_1, CUR_2, null, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFarDate() {
    new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, null, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeWrongDateDeprecated() {
    FX_SWAP_DEFINITION_FIN.toDerivative(FAR_DATE.plusDays(1), "A", "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeWrongDate() {
    FX_SWAP_DEFINITION_FIN.toDerivative(FAR_DATE.plusDays(1));
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(FX_NEAR_DEFINITION, FX_SWAP_DEFINITION_LEG.getNearLeg());
    assertEquals(FX_FAR_DEFINITION, FX_SWAP_DEFINITION_LEG.getFarLeg());
    assertEquals(FX_NEAR_DEFINITION, FX_SWAP_DEFINITION_FIN.getNearLeg());
    assertEquals(FX_FAR_DEFINITION, FX_SWAP_DEFINITION_FIN.getFarLeg());
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    assertTrue(FX_SWAP_DEFINITION_LEG.equals(FX_SWAP_DEFINITION_LEG));
    final ForexSwapDefinition newFxSwap = new ForexSwapDefinition(FX_NEAR_DEFINITION, FX_FAR_DEFINITION);
    assertTrue(FX_SWAP_DEFINITION_LEG.equals(newFxSwap));
    assertTrue(FX_SWAP_DEFINITION_FIN.equals(newFxSwap));
    assertTrue(FX_SWAP_DEFINITION_LEG.hashCode() == newFxSwap.hashCode());
    ForexSwapDefinition modifiedFxSwap;
    modifiedFxSwap = new ForexSwapDefinition(FX_FAR_DEFINITION, FX_FAR_DEFINITION);
    assertFalse(FX_SWAP_DEFINITION_LEG.equals(modifiedFxSwap));
    modifiedFxSwap = new ForexSwapDefinition(FX_NEAR_DEFINITION, FX_NEAR_DEFINITION);
    assertFalse(FX_SWAP_DEFINITION_LEG.equals(modifiedFxSwap));
    assertFalse(FX_SWAP_DEFINITION_LEG.equals(CUR_1));
    assertFalse(FX_SWAP_DEFINITION_LEG.equals(null));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivativeDeprecated() {
    final String eur = "Discounting EUR";
    final String usd = "Discounting USD";
    final String[] names = new String[] {eur, usd};
    final Forex fxNear = FX_NEAR_DEFINITION.toDerivative(REFERENCE_DATE, names);
    final Forex fxFar = FX_FAR_DEFINITION.toDerivative(REFERENCE_DATE, names);
    final ForexSwap fxSwapExpected = new ForexSwap(fxNear, fxFar);
    final InstrumentDerivative fxSwap = FX_SWAP_DEFINITION_FIN.toDerivative(REFERENCE_DATE, names);
    assertEquals(fxSwapExpected, fxSwap);
  }

  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivative() {
    final Forex fxNear = FX_NEAR_DEFINITION.toDerivative(REFERENCE_DATE);
    final Forex fxFar = FX_FAR_DEFINITION.toDerivative(REFERENCE_DATE);
    final ForexSwap fxSwapExpected = new ForexSwap(fxNear, fxFar);
    final InstrumentDerivative fxSwap = FX_SWAP_DEFINITION_FIN.toDerivative(REFERENCE_DATE);
    assertEquals(fxSwapExpected, fxSwap);
  }
}
