/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * Tests related to the construction of price index.
 */
public class IndexPriceTest {

  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final Currency REGION = Currency.EUR;
  private static final Period LAG = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR, REGION, LAG);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new IndexPrice(null, CUR, REGION, LAG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IndexPrice(NAME, null, REGION, LAG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegion() {
    new IndexPrice(NAME, CUR, null, LAG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLag() {
    new IndexPrice(NAME, CUR, REGION, null);
  }

  @Test
  public void getter() {
    assertEquals("Price Index: getter: name", NAME, PRICE_INDEX.getName());
    assertEquals("Price Index: getter: currency", CUR, PRICE_INDEX.getCurrency());
    assertEquals("Price Index: getter: region", REGION, PRICE_INDEX.getRegion());
    assertEquals("Price Index: getter: lag", LAG, PRICE_INDEX.getPublicationLag());
  }

  @Test
  public void testEqualHash() {
    assertEquals(PRICE_INDEX, PRICE_INDEX);
    IndexPrice indexDuplicate = new IndexPrice("Euro HICP x", CUR, REGION, LAG);
    assertEquals(PRICE_INDEX, indexDuplicate);
    assertEquals(PRICE_INDEX.hashCode(), indexDuplicate.hashCode());
    IndexPrice modified;
    modified = new IndexPrice("xxx", CUR, REGION, LAG);
    assertFalse(PRICE_INDEX.equals(modified));
    modified = new IndexPrice(NAME, Currency.AUD, REGION, LAG);
    assertFalse(PRICE_INDEX.equals(modified));
    modified = new IndexPrice(NAME, CUR, Currency.AUD, LAG);
    assertFalse(PRICE_INDEX.equals(modified));
  }
}
