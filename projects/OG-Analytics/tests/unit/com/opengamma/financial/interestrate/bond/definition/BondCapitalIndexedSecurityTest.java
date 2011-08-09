/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponFirstOfMonthDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Tests the construction of Inflation Capital index bonds.
 */
public class BondCapitalIndexedSecurityTest {
  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME = "UK RPI";
  private static final Currency CUR = Currency.GBP;
  private static final Currency REGION = Currency.GBP;
  private static final Period LAG = Period.ofDays(14);
  private static final PriceIndex PRICE_INDEX = new PriceIndex(NAME, CUR, REGION, LAG);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ZonedDateTime START_DATE = DateUtil.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtil.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE = DateUtil.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG = 8;
  private static final double INDEX_START = 173.60; // November 2001 
  private static final double REAL_RATE = 0.02;
  private static final Period COUPON_PERIOD = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final ZonedDateTime PRICING_DATE = DateUtil.getUTCDate(2011, 8, 8);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(PRICING_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> BOND_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(
      PRICE_INDEX, MONTH_LAG, START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ISSUER_UK);
  private static final double SETTLEMENT_TIME = TimeCalculator.getTimeBetween(PRICING_DATE, SPOT_DATE);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MarketDataSets.ukRpiFrom2010();
  @SuppressWarnings("unchecked")
  private static final GenericAnnuity<Coupon> NOMINAL = (GenericAnnuity<Coupon>) BOND_SECURITY_DEFINITION.getNominal().toDerivative(PRICING_DATE, "Not used");
  @SuppressWarnings("unchecked")
  private static final GenericAnnuity<Coupon> COUPON = (GenericAnnuity<Coupon>) BOND_SECURITY_DEFINITION.getCoupon().toDerivative(PRICING_DATE, UK_RPI, "Not used");
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY = new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, YIELD_CONVENTION, ISSUER_UK);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondCapitalIndexedSecurity<Coupon>(null, COUPON, SETTLEMENT_TIME, YIELD_CONVENTION, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, null, SETTLEMENT_TIME, YIELD_CONVENTION, ISSUER_UK);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondCapitalIndexedSecurity<Coupon>(NOMINAL, COUPON, SETTLEMENT_TIME, null, ISSUER_UK);
  }

  @Test
  public void getter() {
    assertEquals("Inflation Capital Indexed bond: getter", YIELD_CONVENTION, BOND_SECURITY.getYieldConvention());
    assertEquals("Inflation Capital Indexed bond: getter", NOMINAL, BOND_SECURITY.getNominal());
    assertEquals("Inflation Capital Indexed bond: getter", COUPON, BOND_SECURITY.getCoupon());
  }

}
