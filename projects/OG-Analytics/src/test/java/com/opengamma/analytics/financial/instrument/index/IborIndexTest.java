/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IborIndexTest {
  //Libor3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final String NAME = "USD LIBOR 3M";
  private static final IborIndex INDEX2 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IborIndex(null, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    new IborIndex(CUR, null, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, null, BUSINESS_DAY, IS_EOM, "Ibor");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, null, IS_EOM, "Ibor");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testName() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, null);
  }

  @Test
  public void getter() {
    assertEquals(INDEX.getCurrency(), CUR);
    assertEquals(INDEX.getTenor(), TENOR);
    assertEquals(INDEX.getSpotLag(), SETTLEMENT_DAYS);
    assertEquals(INDEX.getDayCount(), DAY_COUNT);
    assertEquals(INDEX.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(INDEX.isEndOfMonth(), IS_EOM);
    final String name = "Ibor";
    assertEquals(name, INDEX.getName());
    assertEquals(name, INDEX.toString());
    assertEquals(NAME, INDEX2.getName());
  }

  @Test
  public void testEqualHash() {
    assertEquals(INDEX, INDEX);
    final IborIndex indexDuplicate = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    assertEquals(INDEX, indexDuplicate);
    assertEquals(INDEX.hashCode(), indexDuplicate.hashCode());
    final IborIndex indexNoEOM1 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, !IS_EOM, "Ibor");
    final IborIndex indexNoEOM2 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, !IS_EOM, "Ibor");
    assertEquals(indexNoEOM1.hashCode(), indexNoEOM2.hashCode());
    final Currency currencyModified = Currency.USD;
    IborIndex indexModified = new IborIndex(currencyModified, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, !IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    final Period tenorModified = Period.ofMonths(2);
    indexModified = new IborIndex(CUR, tenorModified, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DayCounts.ACT_365, BUSINESS_DAY, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventions.FOLLOWING, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    final Currency otherCurrency = Currency.USD;
    indexModified = new IborIndex(otherCurrency, TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    final Period otherTenor = Period.ofMonths(6);
    indexModified = new IborIndex(CUR, otherTenor, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, otherTenor, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor1");
    assertFalse(INDEX.equals(indexModified));
    assertFalse(INDEX.equals(null));
    assertFalse(INDEX.equals(CUR));
  }

}
