/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

/**
 * 
 */
public class CurrencyAmountTest {
  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final double A1 = 100;
  private static final double A2 = 200;
  private static final CurrencyAmount CCY_AMOUNT = CurrencyAmount.of(CCY1, A1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    new CurrencyAmount(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    CurrencyAmount.of(null, A1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullOther() {
    CCY_AMOUNT.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddWrongCurrency() {
    CCY_AMOUNT.add(CurrencyAmount.of(CCY2, A2));
  }

  @Test
  public void testObject() {
    assertEquals(CCY_AMOUNT.getAmount(), A1, 0);
    assertEquals(CCY_AMOUNT.getCurrency(), CCY1);
    CurrencyAmount other = CurrencyAmount.of(CCY1, A1);
    assertEquals(CCY_AMOUNT, other);
    assertEquals(CCY_AMOUNT.hashCode(), other.hashCode());
    other = new CurrencyAmount(CCY1, A1);
    assertEquals(CCY_AMOUNT, other);
    assertEquals(CCY_AMOUNT.hashCode(), other.hashCode());
    other = CurrencyAmount.of(CCY2, A1);
    assertFalse(CCY_AMOUNT.equals(other));
    other = CurrencyAmount.of(CCY1, A2);
    assertFalse(CCY_AMOUNT.equals(other));
  }

  @Test
  public void testArithmetic() {
    final CurrencyAmount ccyAmount = CurrencyAmount.of(CCY1, A2);
    final CurrencyAmount sum = CCY_AMOUNT.add(ccyAmount);
    assertEquals(sum.getCurrency(), CCY1);
    assertEquals(sum.getAmount(), A1 + A2);
    final CurrencyAmount scaled = CCY_AMOUNT.scale(A2);
    assertEquals(scaled.getCurrency(), CCY1);
    assertEquals(scaled.getAmount(), A1 * A2);
  }
}
