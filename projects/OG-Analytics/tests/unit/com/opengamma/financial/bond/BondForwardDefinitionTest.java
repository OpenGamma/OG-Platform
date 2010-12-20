/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;

/**
 * 
 */
public class BondForwardDefinitionTest {
  private static final LocalDate[] NOMINAL_DATES = new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1), LocalDate.of(2010, 3, 1), LocalDate.of(2010, 4, 1), LocalDate.of(2010, 5, 1),
      LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1), LocalDate.of(2010, 11, 1), LocalDate.of(2010, 12, 1)};
  private static final LocalDate[] SETTLEMENT_DATES = new LocalDate[] {LocalDate.of(2010, 1, 5), LocalDate.of(2010, 2, 3), LocalDate.of(2010, 3, 3), LocalDate.of(2010, 4, 5),
      LocalDate.of(2010, 5, 5), LocalDate.of(2010, 6, 3), LocalDate.of(2010, 7, 5), LocalDate.of(2010, 8, 4), LocalDate.of(2010, 9, 3), LocalDate.of(2010, 10, 5), LocalDate.of(2010, 11, 3),
      LocalDate.of(2010, 12, 3)};
  private static final BondConvention BOND_CONVENTION = new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond", 0, SimpleYieldConvention.US_TREASURY_EQUIVALANT);
  private static final double COUPON = 0.04;
  private static final double[] COUPONS = new double[] {0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04};
  private static final double COUPONS_PER_YEAR = 12;
  private static final double NOTIONAL = 100;
  private static final BondDefinition BOND_DEFINITION = new BondDefinition(NOMINAL_DATES, SETTLEMENT_DATES, COUPONS, NOTIONAL, COUPONS_PER_YEAR, BOND_CONVENTION);
  private static final LocalDate FORWARD_DATE = LocalDate.of(2010, 2, 15);
  private static final BondConvention BOND_FORWARD_CONVENTION = new BondConvention(1, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new MondayToFridayCalendar("Weekend"), true, "USD Bond Forward", 0, SimpleYieldConvention.MONEY_MARKET);
  private static final BondForwardDefinition BOND_FORWARD_DEFINITION = new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, BOND_FORWARD_CONVENTION);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBondDefinition() {
    new BondForwardDefinition(null, FORWARD_DATE, BOND_FORWARD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardDate() {
    new BondForwardDefinition(BOND_DEFINITION, null, BOND_FORWARD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention() {
    new BondForwardDefinition(BOND_DEFINITION, FORWARD_DATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFprwardDate() {
    new BondForwardDefinition(BOND_DEFINITION, LocalDate.of(2012, 1, 1), BOND_FORWARD_CONVENTION);
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

  //@Test(expected = IllegalArgumentException.class)
  public void testToBondForward() {

  }
}
