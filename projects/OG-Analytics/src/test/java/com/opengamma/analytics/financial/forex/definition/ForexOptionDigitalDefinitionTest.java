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
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Digital Forex options (definition version).
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionDigitalDefinitionTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2012, 6, 8);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 6, 12);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final boolean PAY_DOM = true;
  private static final ForexOptionDigitalDefinition FX_OPTION_DEFINITION = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG, PAY_DOM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFX() {
    new ForexOptionDigitalDefinition(null, EXPIRATION_DATE, IS_CALL, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiration() {
    new ForexOptionDigitalDefinition(FX_DEFINITION, null, IS_CALL, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDate() {
    final ZonedDateTime expirationDateWrong = DateUtils.getUTCDate(2012, 6, 13);
    new ForexOptionDigitalDefinition(FX_DEFINITION, expirationDateWrong, IS_CALL, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDate2() {
    final ZonedDateTime expirationDateWrong = DateUtils.getUTCDate(2012, 6, 13);
    new ForexOptionDigitalDefinition(FX_DEFINITION, expirationDateWrong, IS_CALL, IS_LONG, PAY_DOM);
  }

  @Test
  public void getter() {
    assertEquals("ForexOptionDigitalDefinition - getter", FX_DEFINITION, FX_OPTION_DEFINITION.getUnderlyingForex());
    assertEquals("ForexOptionDigitalDefinition - getter", EXPIRATION_DATE, FX_OPTION_DEFINITION.getExpirationDate());
    assertEquals("ForexOptionDigitalDefinition - getter", IS_CALL, FX_OPTION_DEFINITION.isCall());
    assertEquals("ForexOptionDigitalDefinition - getter", IS_LONG, FX_OPTION_DEFINITION.isLong());
    assertEquals("ForexOptionDigitalDefinition - getter", PAY_DOM, FX_OPTION_DEFINITION.payDomestic());
  }

  @Test
  public void defaultPayCurrency() {
    assertEquals("ForexOptionDigitalDefinition - constructor", true, new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG, PAY_DOM).payDomestic());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue("ForexOptionDigitalDefinition: equal/hash code", FX_OPTION_DEFINITION.equals(FX_OPTION_DEFINITION));
    final ForexOptionDigitalDefinition otherOption = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
    assertTrue("ForexOptionDigitalDefinition: equal/hash code", otherOption.equals(FX_OPTION_DEFINITION));
    assertEquals("ForexOptionDigitalDefinition: equal/hash code", FX_OPTION_DEFINITION.hashCode(), otherOption.hashCode());
    final ForexOptionDigitalDefinition put1 = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL, !IS_LONG);
    final ForexOptionDigitalDefinition put2 = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL, !IS_LONG);
    assertEquals("ForexOptionDigitalDefinition: equal/hash code", put1.hashCode(), put2.hashCode());
    final ForexOptionDigitalDefinition put3 = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL, !IS_LONG, !PAY_DOM);
    final ForexOptionDigitalDefinition put4 = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL, !IS_LONG, !PAY_DOM);
    assertEquals("ForexOptionDigitalDefinition: equal/hash code", put3.hashCode(), put4.hashCode());
    ForexOptionDigitalDefinition modifiedOption;
    modifiedOption = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, !IS_CALL, IS_LONG);
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", modifiedOption.equals(FX_OPTION_DEFINITION));
    modifiedOption = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, !IS_LONG);
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", modifiedOption.equals(FX_OPTION_DEFINITION));
    modifiedOption = new ForexOptionDigitalDefinition(FX_DEFINITION, PAYMENT_DATE, IS_CALL, IS_LONG);
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", modifiedOption.equals(FX_OPTION_DEFINITION));
    modifiedOption = new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG, !PAY_DOM);
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", modifiedOption.equals(FX_OPTION_DEFINITION));
    final ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    modifiedOption = new ForexOptionDigitalDefinition(modifiedFxDefinition, EXPIRATION_DATE, IS_CALL, IS_LONG);
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", modifiedOption.equals(FX_OPTION_DEFINITION));
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", FX_OPTION_DEFINITION.equals(CUR_1));
    assertFalse("ForexOptionDigitalDefinition: equal/hash code", FX_OPTION_DEFINITION.equals(null));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivativeDeprecated() {
    final String discountingEUR = "Discounting EUR";
    final String discountingUSD = "Discounting USD";
    final String[] curves_name = new String[] {discountingEUR, discountingUSD};
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 20);
    final InstrumentDerivative optionConverted = FX_OPTION_DEFINITION.toDerivative(referenceDate, curves_name);
    final Forex fx = FX_DEFINITION.toDerivative(referenceDate, curves_name);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double expirationTime = actAct.getDayCountFraction(referenceDate, EXPIRATION_DATE);
    final ForexOptionDigital optionConstructed = new ForexOptionDigital(fx, expirationTime, IS_CALL, IS_LONG, PAY_DOM);
    assertEquals("Convertion to derivative", optionConstructed, optionConverted);
  }

  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivative() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 20);
    final InstrumentDerivative optionConverted = FX_OPTION_DEFINITION.toDerivative(referenceDate);
    final Forex fx = FX_DEFINITION.toDerivative(referenceDate);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double expirationTime = actAct.getDayCountFraction(referenceDate, EXPIRATION_DATE);
    final ForexOptionDigital optionConstructed = new ForexOptionDigital(fx, expirationTime, IS_CALL, IS_LONG, PAY_DOM);
    assertEquals("Convertion to derivative", optionConstructed, optionConverted);
  }
}
