/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import java.util.Arrays;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BondForwardDefinitionTest {
  private static final LocalDate[] NOMINAL_DATES = new LocalDate[] {LocalDate.of(2000, 1, 12), LocalDate.of(2000, 1, 12), LocalDate.of(2000, 7, 12), LocalDate.of(2001, 1, 12),
      LocalDate.of(2001, 7, 12), LocalDate.of(2002, 1, 12), LocalDate.of(2002, 7, 12), LocalDate.of(2003, 1, 12), LocalDate.of(2003, 7, 12), LocalDate.of(2004, 1, 12), LocalDate.of(2004, 7, 12),
      LocalDate.of(2005, 1, 12), LocalDate.of(2005, 7, 12), LocalDate.of(2006, 1, 12), LocalDate.of(2006, 7, 12), LocalDate.of(2007, 1, 12), LocalDate.of(2007, 7, 12), LocalDate.of(2008, 1, 12),
      LocalDate.of(2008, 7, 12), LocalDate.of(2009, 1, 12), LocalDate.of(2009, 7, 12), LocalDate.of(2010, 1, 12), LocalDate.of(2010, 7, 12), LocalDate.of(2011, 1, 12), LocalDate.of(2011, 7, 12)};
  private static final LocalDate[] SETTLEMENT_DATES = new LocalDate[] {LocalDate.of(2000, 1, 13), LocalDate.of(2000, 1, 13), LocalDate.of(2000, 7, 13), LocalDate.of(2001, 1, 15),
      LocalDate.of(2001, 7, 13), LocalDate.of(2002, 1, 14), LocalDate.of(2002, 7, 15), LocalDate.of(2003, 1, 13), LocalDate.of(2003, 7, 14), LocalDate.of(2004, 1, 13), LocalDate.of(2004, 7, 13),
      LocalDate.of(2005, 1, 13), LocalDate.of(2005, 7, 13), LocalDate.of(2006, 1, 13), LocalDate.of(2006, 7, 13), LocalDate.of(2007, 1, 15), LocalDate.of(2007, 7, 13), LocalDate.of(2008, 1, 14),
      LocalDate.of(2008, 7, 14), LocalDate.of(2009, 1, 13), LocalDate.of(2009, 7, 13), LocalDate.of(2010, 1, 13), LocalDate.of(2010, 7, 13), LocalDate.of(2011, 1, 13), LocalDate.of(2011, 7, 13)};
  private static final BondConvention BOND_CONVENTION = new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "UK Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT);
  private static final double COUPON = 0.0625;
  private static final double[] COUPONS = new double[NOMINAL_DATES.length - 1];
  private static final double COUPONS_PER_YEAR = 2;
  private static final double NOTIONAL = 100;
  private static final BondDefinition BOND_DEFINITION = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final LocalDate FORWARD_DATE = LocalDate.of(2000, 6, 30);
  private static final BondConvention BOND_FORWARD_CONVENTION;
  private static final BondForwardDefinition BOND_FORWARD_DEFINITION;

  static {
    Arrays.fill(COUPONS, COUPON);
    BOND_FORWARD_CONVENTION = new BondConvention(0, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        new MondayToFridayCalendar("Weekend"), true, "UK Bond Forward", 0, SimpleYieldConvention.MONEY_MARKET);
    BOND_FORWARD_DEFINITION = new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, BOND_FORWARD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondDefinition() {
    new BondForwardDefinition(null, FORWARD_DATE, BOND_FORWARD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardDate() {
    new BondForwardDefinition(BOND_DEFINITION, null, BOND_FORWARD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFprwardDate() {
    new BondForwardDefinition(BOND_DEFINITION, LocalDate.of(2012, 1, 1), BOND_FORWARD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToDerivativeWithNullDate() {
    BOND_FORWARD_DEFINITION.toDerivative(null, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToDerivativeWithNullNames() {
    BOND_FORWARD_DEFINITION.toDerivative(FORWARD_DATE, (String[]) null);
  }

  @Test
  public void testGetters() {
    assertEquals(BOND_FORWARD_DEFINITION.getConvention(), BOND_FORWARD_CONVENTION);
    assertEquals(BOND_FORWARD_DEFINITION.getForwardDate(), FORWARD_DATE);
    assertEquals(BOND_FORWARD_DEFINITION.getUnderlyingBond(), BOND_DEFINITION);
  }

  @Test
  public void testHashCodeAndEquals() {
    BondForwardDefinition other = new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, BOND_FORWARD_CONVENTION);
    assertEquals(BOND_FORWARD_DEFINITION, other);
    assertEquals(BOND_FORWARD_DEFINITION.hashCode(), other.hashCode());
    other = new BondForwardDefinition(new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPON + 1, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION), FORWARD_DATE, BOND_FORWARD_CONVENTION);
    assertFalse(BOND_FORWARD_DEFINITION.equals(other));
    other = new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE.plusDays(1), BOND_FORWARD_CONVENTION);
    assertFalse(BOND_FORWARD_DEFINITION.equals(other));
    other = new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, BOND_CONVENTION);
    assertFalse(BOND_FORWARD_DEFINITION.equals(other));
  }

  @Test
  public void testToBondForward() {
    final LocalDate tradeDate = LocalDate.of(2000, 3, 20);
    final LocalDate deliveredBondSettlementDate = LocalDate.of(2000, 3, 22);
    final BondForward forward = BOND_FORWARD_DEFINITION.toDerivative(tradeDate, "A");
    final double lastCouponToBondSettlement = DateUtil.getDaysBetween(LocalDate.of(2000, 1, 12), deliveredBondSettlementDate);
    assertEquals(lastCouponToBondSettlement, 70, 0);
    final double daysBetweenCoupons = DateUtil.getDaysBetween(LocalDate.of(2000, 1, 12), LocalDate.of(2000, 7, 12));
    assertEquals(daysBetweenCoupons, 182, 0);
    assertEquals(forward.getAccruedInterest(), COUPON * lastCouponToBondSettlement / daysBetweenCoupons / COUPONS_PER_YEAR, 0);
    final double lastCouponToForwardSettlement = DateUtil.getDaysBetween(LocalDate.of(2000, 1, 12), FORWARD_DATE);
    assertEquals(lastCouponToForwardSettlement, 170, 0);
    assertEquals(forward.getAccruedInterestAtDelivery(), COUPON * lastCouponToForwardSettlement / daysBetweenCoupons / COUPONS_PER_YEAR, 0);
  }
}
