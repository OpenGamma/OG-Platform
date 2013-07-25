/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test CurrencyAmount.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyAmountTest {

  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final double A1 = 100;
  private static final double A2 = 200;
  private static final CurrencyAmount CCY_AMOUNT = CurrencyAmount.of(CCY1, A1);

  @Test
  public void test_fixture() {
    assertEquals(CCY1, CCY_AMOUNT.getCurrency());
    assertEquals(A1, CCY_AMOUNT.getAmount(), 0);
  }

  //-------------------------------------------------------------------------
  // factories
  //-------------------------------------------------------------------------
  public void test_of_Currency() {
    CurrencyAmount test = CurrencyAmount.of(Currency.USD, A1);
    assertEquals(Currency.USD, test.getCurrency());
    assertEquals(A1, test.getAmount(), 0.0001d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_Currency_nullCurrency() {
    CurrencyAmount.of((Currency) null, A1);
  }

  //-------------------------------------------------------------------------
  public void test_of_String() {
    CurrencyAmount test = CurrencyAmount.of("USD", A1);
    assertEquals(Currency.USD, test.getCurrency());
    assertEquals(A1, test.getAmount(), 0.0001d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_nullCurrency() {
    CurrencyAmount.of((String) null, A1);
  }

  //-------------------------------------------------------------------------
  // parse(String)
  //-------------------------------------------------------------------------
  @Test
  public void test_parse_String() {
    assertEquals(CurrencyAmount.of(Currency.AUD, 100.001), CurrencyAmount.parse("AUD 100.001"));
    assertEquals(CurrencyAmount.of(Currency.AUD, 123.3), CurrencyAmount.parse("AUD 123.3"));
    assertEquals(CCY_AMOUNT, CurrencyAmount.parse(CCY_AMOUNT.toString()));
  }

  @DataProvider(name = "badParse")
  Object[][] data_badParse() {
    return new Object[][] {
      {"AUD"},
      {"AUD aa"},
      {"123"},
      {null},
    };
  }
  @Test(dataProvider = "badParse", expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_bad(String input) {
    CurrencyAmount.parse(input);
  }

  //-------------------------------------------------------------------------
  public void test_plus() {
    final CurrencyAmount ccyAmount = CurrencyAmount.of(CCY1, A2);
    CurrencyAmount test = CCY_AMOUNT.plus(ccyAmount);
    assertEquals(CCY1, test.getCurrency());
    assertEquals(A1 + A2, test.getAmount());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_plus_addNullOther() {
    CCY_AMOUNT.plus(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_plus_wrongCurrency() {
    CCY_AMOUNT.plus(CurrencyAmount.of(CCY2, A2));
  }

  //-------------------------------------------------------------------------
  public void test_multipliedBy() {
    CurrencyAmount test = CCY_AMOUNT.multipliedBy(3.5);
    assertEquals(CCY1, test.getCurrency());
    assertEquals(A1 * 3.5, test.getAmount());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testObject() {
    CurrencyAmount other = CurrencyAmount.of(CCY1, A1);
    assertTrue(CCY_AMOUNT.equals(CCY_AMOUNT));
    assertTrue(CCY_AMOUNT.equals(other));
    assertTrue(other.equals(CCY_AMOUNT));
    assertEquals(other.hashCode(), CCY_AMOUNT.hashCode());
    other = CurrencyAmount.of(CCY1, A1);
    assertEquals(other, CCY_AMOUNT);
    assertEquals(other.hashCode(), CCY_AMOUNT.hashCode());
    other = CurrencyAmount.of(CCY2, A1);
    assertFalse(CCY_AMOUNT.equals(other));
    other = CurrencyAmount.of(CCY1, A2);
    assertFalse(CCY_AMOUNT.equals(other));
  }

  @Test
  public void testEqualsRubbish() {
    assertFalse(CCY_AMOUNT.equals(""));
    assertFalse(CCY_AMOUNT.equals(null));
  }

}
