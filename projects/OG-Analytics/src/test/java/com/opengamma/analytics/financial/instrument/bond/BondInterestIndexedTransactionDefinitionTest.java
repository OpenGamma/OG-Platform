/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
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
public class BondInterestIndexedTransactionDefinitionTest {

  // Index-Linked Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final String NAME_INDEX_UK = "UK RPI";
  private static final IndexPrice PRICE_INDEX_UKRPI = new IndexPrice(NAME_INDEX_UK, Currency.GBP);
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_GILT_1 = DayCounts.ACT_ACT_ICMA;
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE_GILT_1 = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime MATURITY_DATE_GILT_1 = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_GILT_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_GILT_1 = 8;
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final double REAL_RATE_GILT_1 = 0.02;
  private static final Period COUPON_PERIOD_GILT_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  private static final String ISSUER_UK = "UK GOVT";
  private static final BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> BOND_GILT_1_SECURITY_DEFINITION = BondInterestIndexedSecurityDefinition
      .fromMonthly(
          PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
          BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK);
  private static final double QUANTITY = 654321;
  private static final ZonedDateTime SETTLE_DATE_GILT_1 = DateUtils.getUTCDate(2011, 8, 10);
  private static final double PRICE_GILT_1 = 1.80;
  private static final BondInterestIndexedTransactionDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> BOND_GILT_1_TRANSACTION_DEFINITION = new BondInterestIndexedTransactionDefinition<>(
      BOND_GILT_1_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_GILT_1, PRICE_GILT_1);

  @Test
  public void getter() {
    assertEquals("Capital Index Bond", QUANTITY, BOND_GILT_1_TRANSACTION_DEFINITION.getQuantity());
  }

  @Test
  public void toDerivative() {
    final DoubleTimeSeries<ZonedDateTime> ukRpi = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 8, 3); // One coupon fixed
    final BondInterestIndexedTransaction<PaymentFixed, Coupon> bondTransactionConverted = BOND_GILT_1_TRANSACTION_DEFINITION.toDerivative(pricingDate, ukRpi);
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> purchase = BOND_GILT_1_SECURITY_DEFINITION.toDerivative(pricingDate, SETTLE_DATE_GILT_1, ukRpi);
    assertEquals("Capital Index Bond: toDerivative", purchase, bondTransactionConverted.getBondTransaction());
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(pricingDate, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP);
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> standard = BOND_GILT_1_SECURITY_DEFINITION.toDerivative(pricingDate, spot, ukRpi);
    assertEquals("Capital Index Bond: toDerivative", standard, bondTransactionConverted.getBondStandard());
    final BondInterestIndexedTransaction<PaymentFixed, Coupon> expected = new BondInterestIndexedTransaction<>(purchase, QUANTITY, PRICE_GILT_1, standard, NOTIONAL_GILT_1);
    assertEquals("Capital Index Bond: toDerivative", expected, bondTransactionConverted);
  }
}
