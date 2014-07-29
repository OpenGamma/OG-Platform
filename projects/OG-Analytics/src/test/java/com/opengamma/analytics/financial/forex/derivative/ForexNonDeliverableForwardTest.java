/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of ForexNonDeliverableForward.
 */
@Test(groups = TestGroup.UNIT)
public class ForexNonDeliverableForwardTest {

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 1000000; // 1m
  private static final double FX_RATE = 1123.45;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);
  private static final double FIXING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);

  private static final String KRW_DSC = "Discounting KRW";
  private static final String USD_DSC = "Discounting USD";

  private static final ForexNonDeliverableForward NDF = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1Deprecated() {
    new ForexNonDeliverableForward(null, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2Deprecated() {
    new ForexNonDeliverableForward(KRW, null, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDateOrderDeprecated() {
    new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, PAYMENT_TIME, FIXING_TIME, KRW_DSC, USD_DSC);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sameCurrencyDeprecated() {
    new ForexNonDeliverableForward(USD, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new ForexNonDeliverableForward(null, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new ForexNonDeliverableForward(KRW, null, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDateOrder() {
    new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, PAYMENT_TIME, FIXING_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sameCurrency() {
    new ForexNonDeliverableForward(USD, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals("Forex NDF getter", KRW, NDF.getCurrency1());
    assertEquals("Forex NDF getter", USD, NDF.getCurrency2());
    assertEquals("Forex NDF getter", FIXING_TIME, NDF.getFixingTime());
    assertEquals("Forex NDF getter", PAYMENT_TIME, NDF.getPaymentTime());
    assertEquals("Forex NDF getter", NOMINAL_USD, NDF.getNotionalCurrency2());
    assertEquals("Forex NDF getter", FX_RATE, NDF.getExchangeRate());
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHashDeprecated() {
    final ForexNonDeliverableForward newNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertTrue(NDF.equals(newNdf));
    assertTrue(NDF.hashCode() == newNdf.hashCode());
    ForexNonDeliverableForward modifiedNdf;
    modifiedNdf = new ForexNonDeliverableForward(Currency.EUR, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, Currency.EUR, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD + 100.0, FX_RATE, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE + 1.0, FIXING_TIME, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME - 0.01, PAYMENT_TIME, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME + 0.01, KRW_DSC, USD_DSC);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    assertFalse(NDF.equals(USD));
    assertFalse(NDF.equals(null));
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    final ForexNonDeliverableForward newNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
    assertTrue(NDF.equals(newNdf));
    assertTrue(NDF.hashCode() == newNdf.hashCode());
    ForexNonDeliverableForward modifiedNdf;
    modifiedNdf = new ForexNonDeliverableForward(Currency.EUR, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, Currency.EUR, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD + 100.0, FX_RATE, FIXING_TIME, PAYMENT_TIME);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE + 1.0, FIXING_TIME, PAYMENT_TIME);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME - 0.01, PAYMENT_TIME);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    modifiedNdf = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_TIME, PAYMENT_TIME + 0.01);
    assertFalse("Forex NDF: equal - hash code", NDF.equals(modifiedNdf));
    assertFalse(NDF.equals(USD));
    assertFalse(NDF.equals(null));
  }
}
