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

import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the construction of ForexNonDeliverableForwardDefinition and it conversion to derivative.
 */
public class ForexNonDeliverableForwardDefinitionTest {

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 1000000; // 1m
  private static final double FX_RATE = 1123.45;
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);
  private static final String KRW_DSC = "Discounting KRW";
  private static final String USD_DSC = "Discounting USD";
  private static final String[] CURVE_NAMES = new String[] {KRW_DSC, USD_DSC};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new ForexNonDeliverableForwardDefinition(null, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new ForexNonDeliverableForwardDefinition(KRW, null, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingDate() {
    new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, null, PAYMENT_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPaymentDate() {
    new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDateOrder() {
    new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, PAYMENT_DATE, FIXING_DATE);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals("Forex NDF getter", KRW, NDF_DEFINITION.getCurrency1());
    assertEquals("Forex NDF getter", USD, NDF_DEFINITION.getCurrency2());
    assertEquals("Forex NDF getter", FIXING_DATE, NDF_DEFINITION.getFixingDate());
    assertEquals("Forex NDF getter", PAYMENT_DATE, NDF_DEFINITION.getPaymentDate());
    assertEquals("Forex NDF getter", NOMINAL_USD, NDF_DEFINITION.getNotional());
    assertEquals("Forex NDF getter", FX_RATE, NDF_DEFINITION.getExchangeRate());
  }

  @Test
  /**
   * Tests the class toDerivative method.
   */
  public void toDerivative() {
    ForexNonDeliverableForward ndfConverted = NDF_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
    ForexNonDeliverableForward ndfExpected = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE), TimeCalculator.getTimeBetween(
        REFERENCE_DATE, PAYMENT_DATE), KRW_DSC, USD_DSC);
    assertEquals("Forex NDF - toDerivatives", ndfExpected, ndfConverted);
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    final ForexNonDeliverableForwardDefinition newNdf = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
    assertTrue(NDF_DEFINITION.equals(newNdf));
    assertTrue(NDF_DEFINITION.hashCode() == newNdf.hashCode());
    ForexNonDeliverableForwardDefinition modifiedNdf;
    modifiedNdf = new ForexNonDeliverableForwardDefinition(Currency.EUR, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForwardDefinition(KRW, Currency.EUR, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD + 100.0, FX_RATE, FIXING_DATE, PAYMENT_DATE);
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE + 1.0, FIXING_DATE, PAYMENT_DATE);
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE.minusDays(1), PAYMENT_DATE);
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE.plusDays(1));
    assertFalse("Forex NDF: equal - hash code", NDF_DEFINITION.equals(modifiedNdf));
    assertFalse(NDF_DEFINITION.equals(USD));
    assertFalse(NDF_DEFINITION.equals(null));
  }

}
