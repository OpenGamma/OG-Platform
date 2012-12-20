/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test UnorderedCurrencyPair.
 */
@Test
public class UnorderedCurrencyPairTest {

  //-----------------------------------------------------------------------
  // of(Currency,Currency)
  //-----------------------------------------------------------------------
  public void test_of_CurrencyCurrency_order1() {
    UnorderedCurrencyPair test = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals("GBPUSD", test.toString());
  }

  public void test_of_CurrencyCurrency_order2() {
    UnorderedCurrencyPair test = UnorderedCurrencyPair.of(Currency.USD, Currency.GBP);
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals("GBPUSD", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_Currency_nullCurrency1() {
    UnorderedCurrencyPair.of((Currency) null, Currency.USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_Currency_nullCurrency2() {
    UnorderedCurrencyPair.of(Currency.USD, (Currency) null);
  }

  //-----------------------------------------------------------------------
  // equals() hashCode()
  //-----------------------------------------------------------------------
  public void test_equals_hashCode() {
    UnorderedCurrencyPair a = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    UnorderedCurrencyPair b = UnorderedCurrencyPair.of(Currency.USD, Currency.GBP);
    UnorderedCurrencyPair c = UnorderedCurrencyPair.of(Currency.USD, Currency.EUR);
    
    assertEquals(a.equals(a), true);
    assertEquals(b.equals(b), true);
    assertEquals(c.equals(c), true);
    
    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
    assertEquals(a.hashCode() == b.hashCode(), true);
    
    assertEquals(a.equals(c), false);
    assertEquals(b.equals(c), false);
  }

  public void test_equals_false() {
    UnorderedCurrencyPair a = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(a.equals(null), false);
    assertEquals(a.equals("String"), false);
    assertEquals(a.equals(new Object()), false);
  }

}
