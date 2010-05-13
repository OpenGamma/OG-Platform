/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.opengamma.financial.Currency;

/**
 * Test Currency.
 */
public class CurrencyTest {

  @Test
  public void test_factory_lookups() {
    Currency c1 = Currency.getInstance("USD");
    assertNotNull(c1);
    assertEquals("USD", c1.getISOCode());
    assertSame(c1, Currency.getInstance("USD"));
  }

  @Test(expected=NullPointerException.class)
  public void test_factory_null() {
    Currency.getInstance(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_tooShort() {
    Currency.getInstance("U");
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_tooLong() {
    Currency.getInstance("USD1");
  }

}
