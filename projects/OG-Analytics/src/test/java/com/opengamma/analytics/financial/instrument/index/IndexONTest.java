/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the IndexOIS class.
 */
@Test(groups = TestGroup.UNIT)
public class IndexONTest {

  // USD OIS
  private static final String US_OIS_NAME = "US OIS";
  private static final Currency US_CUR = Currency.USD;
  private static final DayCount US_DAY_COUNT = DayCounts.ACT_360;
  private static final int US_PUBLICATION_LAG = 1;
  private static final IndexON US_OIS = new IndexON(US_OIS_NAME, US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new IndexON(null, US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IndexON(US_OIS_NAME, null, US_DAY_COUNT, US_PUBLICATION_LAG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IndexON(US_OIS_NAME, US_CUR, null, US_PUBLICATION_LAG);
  }


  @Test
  public void getterUS() {
    assertEquals(US_OIS.getName(), US_OIS_NAME);
    assertEquals(US_OIS.getCurrency(), US_CUR);
    assertEquals(US_OIS.getDayCount(), US_DAY_COUNT);
    assertEquals(US_OIS.getPublicationLag(), US_PUBLICATION_LAG);
  }

  @Test
  public void getterEUR() {
    assertEquals(EUR_OIS.getName(), EUR_OIS_NAME);
    assertEquals(EUR_OIS.getCurrency(), EUR_CUR);
    assertEquals(EUR_OIS.getDayCount(), EUR_DAY_COUNT);
    assertEquals(EUR_OIS.getPublicationLag(), EUR_PUBLICATION_LAG);
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("OIS Index: equal/hash code", US_OIS, US_OIS);
    assertFalse("OIS Index: equal/hash code", US_OIS.equals(EUR_OIS));
    final IndexON other = new IndexON(US_OIS_NAME, US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);
    assertEquals("OIS Index: equal/hash code", US_OIS, other);
    assertEquals("OIS Index: equal/hash code", US_OIS.hashCode(), other.hashCode());
    IndexON modified;
    modified = new IndexON("test", US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);
    assertFalse("OIS Index: equal/hash code", US_OIS.equals(modified));
    modified = new IndexON(US_OIS_NAME, EUR_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);
    assertFalse("OIS Index: equal/hash code", US_OIS.equals(modified));
    modified = new IndexON(US_OIS_NAME, US_CUR, DayCounts.ACT_365, US_PUBLICATION_LAG);
    assertFalse("OIS Index: equal/hash code", US_OIS.equals(modified));
    modified = new IndexON(US_OIS_NAME, US_CUR, US_DAY_COUNT, 0);
    assertFalse("OIS Index: equal/hash code", US_OIS.equals(modified));
  }

}
