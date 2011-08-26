/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class BondFutureDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final LocalDate[] NOMINAL_DATES = new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), LocalDate.of(2010, 3, 1), LocalDate.of(2010, 4, 1), LocalDate.of(2010, 5, 1),
      LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1), LocalDate.of(2010, 11, 1), LocalDate.of(2010, 12, 1)};
  private static final LocalDate[] SETTLEMENT_DATES = new LocalDate[] {LocalDate.of(2010, 1, 5), LocalDate.of(2010, 2, 3), LocalDate.of(2010, 3, 3), LocalDate.of(2010, 4, 5),
      LocalDate.of(2010, 5, 5), LocalDate.of(2010, 6, 3), LocalDate.of(2010, 7, 5), LocalDate.of(2010, 8, 4), LocalDate.of(2010, 9, 3), LocalDate.of(2010, 10, 5), LocalDate.of(2010, 11, 3),
      LocalDate.of(2010, 12, 3)};
  private static final BondConvention BOND_CONVENTION = new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT);
  private static final double COUPON = 0.04;
  private static final double COUPONS_PER_YEAR = 12;
  private static final double NOTIONAL = 100;
  private static final BondDefinition BOND_DEFINITION1 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final BondDefinition BOND_DEFINITION2 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON + 1, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final BondDefinition BOND_DEFINITION3 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON + 2, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final BondDefinition BOND_DEFINITION4 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON + 1, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final LocalDate DELIVERY_DATE = LocalDate.of(2010, 6, 15);
  private static final ZonedDateTime ZONED_DELIVERY_DATE = DateUtils.getUTCDate(2010, 6, 15);
  private static final BondConvention BOND_FUTURE_CONVENTION = new BondConvention(0, DayCountFactory.INSTANCE.getDayCount("Actual/365"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond Future", 0, SimpleYieldConvention.MONEY_MARKET);
  private static final double[] CONVERSION_FACTORS = new double[] {1, .8, .6, .4};
  private static final BondDefinition[] DELIVERABLES = new BondDefinition[] {BOND_DEFINITION1, BOND_DEFINITION2, BOND_DEFINITION3, BOND_DEFINITION4};
  private static final BondFutureDefinition BOND_FUTURE_DEFINITION = new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
  private static final double FUTURE_PRICE = 104;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeliverables() {
    new BondFutureDefinition(null, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeliverable() {
    new BondFutureDefinition(new BondDefinition[] {null}, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConversionFactors() {
    new BondFutureDefinition(DELIVERABLES, null, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, null, DELIVERY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeliveryDate() {
    new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadArrayLength() {
    new BondFutureDefinition(DELIVERABLES, new double[] {1, 2, 3}, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToDerivativeNullDate() {
    BOND_FUTURE_DEFINITION.toDerivative(null, FUTURE_PRICE, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToDerivativeNullNames() {
    BOND_FUTURE_DEFINITION.toDerivative(ZONED_DELIVERY_DATE, FUTURE_PRICE, (String[]) null);
  }

  @Test
  public void testGetters() {
    assertEquals(BOND_FUTURE_DEFINITION.getConvention(), BOND_FUTURE_CONVENTION);
    assertArrayEquals(BOND_FUTURE_DEFINITION.getConversionFactors(), CONVERSION_FACTORS, 0);
    assertArrayEquals(BOND_FUTURE_DEFINITION.getDeliverableBonds(), DELIVERABLES);
    assertEquals(BOND_FUTURE_DEFINITION.getDeliveryDate(), DELIVERY_DATE);
  }

  @Test
  public void testHashCodeAndEquals() {
    BondFutureDefinition other = new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
    assertEquals(other, BOND_FUTURE_DEFINITION);
    assertEquals(other.hashCode(), BOND_FUTURE_DEFINITION.hashCode());
    other = new BondFutureDefinition(new BondDefinition[] {BOND_DEFINITION1, BOND_DEFINITION1, BOND_DEFINITION3, BOND_DEFINITION4}, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
    assertFalse(other.equals(BOND_FUTURE_DEFINITION));
    other = new BondFutureDefinition(DELIVERABLES, new double[] {1, 1, 1, 1}, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
    assertFalse(other.equals(BOND_FUTURE_DEFINITION));
    other = new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, new BondConvention(0, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond Future", 0, SimpleYieldConvention.JGB_SIMPLE),
        DELIVERY_DATE);
    assertFalse(other.equals(BOND_FUTURE_DEFINITION));
    other = new BondFutureDefinition(DELIVERABLES, CONVERSION_FACTORS, BOND_FUTURE_CONVENTION, DELIVERY_DATE.plusDays(1));
    assertFalse(other.equals(BOND_FUTURE_DEFINITION));
  }

  @Test
  public void testToDefinition() {
    final ZonedDateTime date = ZONED_DELIVERY_DATE.minusMonths(2);
    final double[] cf = new double[] {1, 0.95};
    final String curveName = "a";
    final BondDefinition b1 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON, COUPONS_PER_YEAR, BOND_CONVENTION);
    final BondDefinition b2 = new BondDefinition(CUR, NOMINAL_DATES, SETTLEMENT_DATES, COUPON * 1.05, COUPONS_PER_YEAR, BOND_CONVENTION);
    final BondForwardDefinition f1 = new BondForwardDefinition(b1, DELIVERY_DATE, BOND_FUTURE_CONVENTION);
    final BondForwardDefinition f2 = new BondForwardDefinition(b2, DELIVERY_DATE, BOND_FUTURE_CONVENTION);
    final BondFutureDefinition definition = new BondFutureDefinition(new BondDefinition[] {b1, b2}, cf, BOND_FUTURE_CONVENTION, DELIVERY_DATE);
    final BondFuture future = new BondFuture(new BondForward[] {f1.toDerivative(date, curveName), f2.toDerivative(date, curveName)}, cf, FUTURE_PRICE);
    assertEquals(future, definition.toDerivative(date, FUTURE_PRICE, curveName));
  }
}
