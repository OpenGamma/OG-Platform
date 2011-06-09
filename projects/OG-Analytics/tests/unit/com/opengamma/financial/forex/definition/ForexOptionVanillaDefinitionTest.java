/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the construction of vanilla Forex options (definition version).
 */
public class ForexOptionVanillaDefinitionTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtil.getUTCDate(2012, 6, 8);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2012, 6, 12);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final boolean IS_CALL = true;
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFX() {
    new ForexOptionVanillaDefinition(null, EXPIRATION_DATE, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiration() {
    new ForexOptionVanillaDefinition(FX_DEFINITION, null, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWrongDate() {
    final ZonedDateTime expirationDateWrong = DateUtil.getUTCDate(2012, 6, 13);
    new ForexOptionVanillaDefinition(FX_DEFINITION, expirationDateWrong, IS_CALL);
  }

  @Test
  public void getter() {
    assertEquals(FX_DEFINITION, FX_OPTION_DEFINITION.getUnderlyingForex());
    assertEquals(EXPIRATION_DATE, FX_OPTION_DEFINITION.getExpirationDate());
    assertEquals(IS_CALL, FX_OPTION_DEFINITION.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    ForexOptionVanillaDefinition otherOption = new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL);
    assertTrue(otherOption.equals(FX_OPTION_DEFINITION));
    assertEquals(FX_OPTION_DEFINITION.hashCode(), otherOption.hashCode());
    ForexOptionVanillaDefinition modifiedOption;
    modifiedOption = new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION_DEFINITION));
    modifiedOption = new ForexOptionVanillaDefinition(FX_DEFINITION, PAYMENT_DATE, IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION_DEFINITION));
    ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    modifiedOption = new ForexOptionVanillaDefinition(modifiedFxDefinition, EXPIRATION_DATE, IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION_DEFINITION));
  }

  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivative() {
    String discountingEUR = "Discounting EUR";
    String discountingUSD = "Discounting USD";
    String[] curves_name = new String[] {discountingEUR, discountingUSD};
    ZonedDateTime referenceDate = DateUtil.getUTCDate(2011, 5, 20);
    ForexDerivative optionConverted = FX_OPTION_DEFINITION.toDerivative(referenceDate, curves_name);
    Forex fx = FX_DEFINITION.toDerivative(referenceDate, curves_name);
    DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    double expirationTime = actAct.getDayCountFraction(referenceDate, EXPIRATION_DATE);
    ForexOptionVanilla optionConstructed = new ForexOptionVanilla(fx, expirationTime, IS_CALL);
    assertEquals("Convertion to derivative", optionConstructed, optionConverted);
  }

}
