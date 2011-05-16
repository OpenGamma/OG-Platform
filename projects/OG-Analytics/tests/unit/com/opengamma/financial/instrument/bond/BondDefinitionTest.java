/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BondDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final LocalDate[] NOMINAL_DATES = new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), LocalDate.of(2010, 3, 1), LocalDate.of(2010, 4, 1), LocalDate.of(2010, 5, 1),
      LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1), LocalDate.of(2010, 11, 1), LocalDate.of(2010, 12, 1)};
  private static final LocalDate[] SETTLEMENT_DATES = new LocalDate[] {LocalDate.of(2010, 1, 5), LocalDate.of(2010, 2, 3), LocalDate.of(2010, 3, 3), LocalDate.of(2010, 4, 5),
      LocalDate.of(2010, 5, 5), LocalDate.of(2010, 6, 3), LocalDate.of(2010, 7, 5), LocalDate.of(2010, 8, 4), LocalDate.of(2010, 9, 3), LocalDate.of(2010, 10, 5), LocalDate.of(2010, 11, 3),
      LocalDate.of(2010, 12, 3)};
  private static final BondConvention CONVENTION = new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT);
  private static final double COUPON = 0.04;
  private static final double[] COUPONS = new double[] {0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04};
  private static final double COUPONS_PER_YEAR = 12;
  private static final double NOTIONAL = 100;
  private static final BondDefinition DEFINITION = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominalDates1() {
    new BondDefinition(CUR, null, SETTLEMENT_DATES, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominalDate1() {
    new BondDefinition(CUR, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyNominalDates1() {
    new BondDefinition(CUR, new LocalDate[] {}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDates1() {
    new BondDefinition(CUR, NOMINAL_DATES, null, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate1() {
    new BondDefinition(CUR, NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDateArrayLength1() {
    new BondDefinition(CUR, NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1)}, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCouponsPerYear1() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, NOTIONAL, -COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention1() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, NOTIONAL, COUPONS_PER_YEAR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominalDates2() {
    new BondDefinition(CUR, null, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominalDate2() {
    new BondDefinition(CUR, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyNominalDates2() {
    new BondDefinition(CUR, new LocalDate[] {}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDates2() {
    new BondDefinition(CUR, NOMINAL_DATES, null, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate2() {
    new BondDefinition(CUR, NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDateArrayLength2() {
    new BondDefinition(CUR, NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1)}, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupons() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, null, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCouponArrayLength() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, new double[] {0.04, 0.04}, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCouponsPerYear2() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, -COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention2() {
    new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConvertAfterExpiry() {
    DEFINITION.toDerivative(ZonedDateTime.of(LocalDateTime.ofMidnight(SETTLEMENT_DATES[SETTLEMENT_DATES.length - 1].plusMonths(1)), TimeZone.UTC), "A");
  }

  @Test
  public void testGetters() {
    assertArrayEquals(DEFINITION.getNominalDates(), NOMINAL_DATES);
    assertArrayEquals(DEFINITION.getSettlementDates(), SETTLEMENT_DATES);
    assertArrayEquals(DEFINITION.getCoupons(), COUPONS, 0);
    assertEquals(DEFINITION.getNotional(), NOTIONAL, 0);
    assertEquals(DEFINITION.getCouponsPerYear(), COUPONS_PER_YEAR, 0);
    assertEquals(DEFINITION.getConvention(), CONVENTION);
  }

  @Test
  public void testHashCodeAndEquals() {
    final double notional = 1;
    final BondDefinition definition = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    BondDefinition other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, notional, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(CUR, SETTLEMENT_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(CUR, NOMINAL_DATES, NOMINAL_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional + 1, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR + 1, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT));
    assertFalse(definition.equals(other));
  }

  @Test
  public void testConversion() {
    final int settlementDays = CONVENTION.getSettlementDays();
    final double deltaAI = 0.04 / 12 / 31;
    ZonedDateTime date;
    Bond bond;
    for (int i = 1; i < 28; i++) {
      date = DateUtil.getUTCDate(2010, 1, i);
      if (CONVENTION.getWorkingDayCalendar().isWorkingDay(date.toLocalDate())) {
        bond = DEFINITION.toDerivative(date, "A");
        final double aI = bond.getAccruedInterest();
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.THURSDAY) {
          assertEquals(aI, deltaAI * (i + settlementDays + 2), EPS);
        } else {
          assertEquals(aI, deltaAI * (i + settlementDays), EPS);
        }
      }
    }
  }
}
