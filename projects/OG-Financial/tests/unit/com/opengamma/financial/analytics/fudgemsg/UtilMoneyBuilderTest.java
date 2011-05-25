/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class UtilMoneyBuilderTest extends AnalyticsTestBase {
  
  @Test
  public void testCurrencyAmount() {
    CurrencyAmount ca1 = CurrencyAmount.of(Currency.AUD, 100);
    CurrencyAmount ca2 = cycleObject(CurrencyAmount.class, ca1);
    assertEquals(ca1, ca2);
  }
  
  @Test
  public void testMultipleCurrencyAmount() {
    CurrencyAmount ca1 = CurrencyAmount.of(Currency.AUD, 100);
    CurrencyAmount ca2 = CurrencyAmount.of(Currency.CAD, 200);
    CurrencyAmount ca3 = CurrencyAmount.of(Currency.CHF, 300);
    MultipleCurrencyAmount mca1 = MultipleCurrencyAmount.of(new CurrencyAmount[]{ca1, ca2, ca3});
    MultipleCurrencyAmount mca2 = cycleObject(MultipleCurrencyAmount.class, mca1);
    assertEquals(mca1, mca2);
  }
}
