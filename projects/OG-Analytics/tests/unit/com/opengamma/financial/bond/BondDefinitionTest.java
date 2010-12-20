/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.bond;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondDefinitionTest {
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
  private static final BondDefinition DEFINITION = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDates1() {
    new BondDefinition(null, SETTLEMENT_DATES, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDate1() {
    new BondDefinition(new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyNominalDates1() {
    new BondDefinition(new LocalDate[] {}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDates1() {
    new BondDefinition(NOMINAL_DATES, null, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDate1() {
    new BondDefinition(NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDateArrayLength1() {
    new BondDefinition(NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1)}, COUPON, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCouponsPerYear1() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPON, NOTIONAL, -COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention1() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPON, NOTIONAL, COUPONS_PER_YEAR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDates2() {
    new BondDefinition(null, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDate2() {
    new BondDefinition(new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyNominalDates2() {
    new BondDefinition(new LocalDate[] {}, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDates2() {
    new BondDefinition(NOMINAL_DATES, null, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDate2() {
    new BondDefinition(NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), null}, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDateArrayLength2() {
    new BondDefinition(NOMINAL_DATES, new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1)}, COUPONS, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCoupons() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, null, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCouponArrayLength() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, new double[] {0.04, 0.04}, NOTIONAL, COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCouponsPerYear2() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, -COUPONS_PER_YEAR, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention2() {
    new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, null);
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
    final BondDefinition definition = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    BondDefinition other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPON, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPON, notional, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, COUPONS_PER_YEAR, CONVENTION);
    assertEquals(definition, other);
    assertEquals(definition.hashCode(), other.hashCode());
    other = new BondDefinition(SETTLEMENT_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(NOMINAL_DATES, NOMINAL_DATES, COUPONS, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, notional, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional + 1, COUPONS_PER_YEAR, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR + 1, CONVENTION);
    assertFalse(definition.equals(other));
    other = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, notional, COUPONS_PER_YEAR, new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT));
    assertFalse(definition.equals(other));
  }

  @Test
  public void testConversion() {
    final int settlementDays = CONVENTION.getSettlementDays();
    final double deltaAI = 0.04 / 12 / 31;
    LocalDate date;
    Bond bond;
    for (int i = 1; i < 32; i++) {
      date = LocalDate.of(2010, 1, i);
      if (CONVENTION.getWorkingDayCalendar().isWorkingDay(date)) {
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
