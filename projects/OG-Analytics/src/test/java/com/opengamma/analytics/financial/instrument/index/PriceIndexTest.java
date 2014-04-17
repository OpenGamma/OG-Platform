/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the construction of price index.
 */
@Test(groups = TestGroup.UNIT)
public class PriceIndexTest {

  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new IndexPrice(null, CUR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IndexPrice(NAME, null);
  }

  @Test
  public void getter() {
    assertEquals("Price Index: getter: name", NAME, PRICE_INDEX.getName());
    assertEquals("Price Index: getter: currency", CUR, PRICE_INDEX.getCurrency());
  }

  @Test
  public void testEqualHash() {
    assertEquals(PRICE_INDEX, PRICE_INDEX);
    IndexPrice indexDuplicate = new IndexPrice("Euro HICP x", CUR);
    assertEquals(PRICE_INDEX, indexDuplicate);
    assertEquals(PRICE_INDEX.hashCode(), indexDuplicate.hashCode());
    IndexPrice modified;
    modified = new IndexPrice("xxx", CUR);
    assertFalse(PRICE_INDEX.equals(modified));
    modified = new IndexPrice(NAME, Currency.AUD);
    assertFalse(PRICE_INDEX.equals(modified));

  }
}
