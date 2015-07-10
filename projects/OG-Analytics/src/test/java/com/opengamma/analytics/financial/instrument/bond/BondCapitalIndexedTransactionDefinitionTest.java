/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedTransactionDefinitionTest {
  // Index-Linked Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME_INDEX_UK = "UK RPI";
  private static final IndexPrice PRICE_INDEX_UKRPI = new IndexPrice(NAME_INDEX_UK, Currency.GBP);
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_GILT_1 = DayCounts.ACT_ACT_ICMA;
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE_GILT_1 = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE_GILT_1 = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE_GILT_1 = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_GILT_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_GILT_1 = 8;
  private static final double INDEX_START_GILT_1 = 173.60; // November 2001
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final double REAL_RATE_GILT_1 = 0.02;
  private static final Period COUPON_PERIOD_GILT_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_GILT_1_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);
  private static final double QUANTITY = 654321;
  private static final ZonedDateTime SETTLE_DATE_GILT_1 = DateUtils.getUTCDate(2011, 8, 10);
  private static final double PRICE_GILT_1 = 1.80;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_GILT_1_TRANSACTION_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
      BOND_GILT_1_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_GILT_1, PRICE_GILT_1);

  @Test
  public void getter() {
    assertEquals("Capital Index Bond", QUANTITY, BOND_GILT_1_TRANSACTION_DEFINITION.getQuantity());
  }

  @Test
  public void toDerivative() {
    final DoubleTimeSeries<ZonedDateTime> ukRpi = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 8, 3); // One coupon fixed
    final BondCapitalIndexedTransaction<Coupon> bondTransactionConverted = BOND_GILT_1_TRANSACTION_DEFINITION.toDerivative(pricingDate, ukRpi);
    final BondCapitalIndexedSecurity<Coupon> purchase = BOND_GILT_1_SECURITY_DEFINITION.toDerivative(pricingDate, SETTLE_DATE_GILT_1, ukRpi);
    assertEquals("Capital Index Bond: toDerivative", purchase, bondTransactionConverted.getBondTransaction());
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(pricingDate, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP);
    final BondCapitalIndexedSecurity<Coupon> standard = BOND_GILT_1_SECURITY_DEFINITION.toDerivative(pricingDate, spot, ukRpi);
    assertEquals("Capital Index Bond: toDerivative", standard, bondTransactionConverted.getBondStandard());
    final BondCapitalIndexedTransaction<Coupon> expected = new BondCapitalIndexedTransaction<>(purchase, QUANTITY, PRICE_GILT_1, standard, NOTIONAL_GILT_1);
    assertEquals("Capital Index Bond: toDerivative", expected, bondTransactionConverted);
  }

  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  //  private static final String NAME_INDEX_US = "US CPI-U";
  //  private static final Period LAG_INDEX_US = Period.ofDays(14);
  //  private static final PriceIndex PRICE_INDEX_USCPI = new PriceIndex(NAME_INDEX_US, Currency.USD, Currency.USD, LAG_INDEX_US);
  //  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  //  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventions.FOLLOWING;
  //  private static final DayCount DAY_COUNT_TIPS_1 = DayCounts.ACT_ACT_ICMA;
  //  private static final boolean IS_EOM_TIPS_1 = false;
  //  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtil.getUTCDate(2006, 1, 15);
  //  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtil.getUTCDate(2016, 1, 15);
  //  private static final YieldConvention YIELD_CONVENTION_TIPS_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  //  private static final int MONTH_LAG_TIPS_1 = 3;
  //  private static final double INDEX_START_TIPS_1 = 198.47742; // Date:
  //  private static final double NOTIONAL_TIPS_1 = 100.00;
  //  private static final double REAL_RATE_TIPS_1 = 0.02;
  //  private static final Period COUPON_PERIOD_TIPS_1 = Period.ofMonths(6);
  //  private static final int SETTLEMENT_DAYS_TIPS_1 = 2;
  //  private static final String ISSUER_US = "US GOVT";
  //  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> BOND_TIPS_1_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition
  //      .fromInterpolation(PRICE_INDEX_USCPI, MONTH_LAG_TIPS_1, START_DATE_TIPS_1, INDEX_START_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1, NOTIONAL_TIPS_1, REAL_RATE_TIPS_1, BUSINESS_DAY_USD,
  //          SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD, DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, ISSUER_US);
  //  private static final double QUANTITY_TIPS_1 = 654321;
  //  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtil.getUTCDate(2011, 8, 10);

}
