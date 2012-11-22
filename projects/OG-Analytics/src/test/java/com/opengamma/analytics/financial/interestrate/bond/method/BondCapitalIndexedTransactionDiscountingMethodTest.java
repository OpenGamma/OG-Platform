/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Tests the present value of Capital inflation indexed bonds.
 */
public class BondCapitalIndexedTransactionDiscountingMethodTest {

  private static final MarketDiscountBundle MARKET = MarketDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MarketDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_USCPI = PRICE_INDEXES[2];
  private static final String[] ISSUER_NAMES = MarketDiscountDataSets.getIssuerNames();
  private static final String ISSUER_US_GOVT = ISSUER_NAMES[0];
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_SECURITY = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_TRANSACTION = new BondCapitalIndexedTransactionDiscountingMethod();
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_TIPS_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_TIPS_1 = false;
  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtils.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtils.getUTCDate(2016, 1, 15);
  private static final YieldConvention YIELD_CONVENTION_TIPS_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_TIPS_1 = 3;
  private static final double INDEX_START_TIPS_1 = 198.47742; // Date: 
  private static final double NOTIONAL_TIPS_1 = 100.00;
  private static final double REAL_RATE_TIPS_1 = 0.02;
  private static final Period COUPON_PERIOD_TIPS_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_TIPS_1 = 2;

  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> BOND_SECURITY_TIPS_1_DEFINITION = BondCapitalIndexedSecurityDefinition
      .fromInterpolation(PRICE_INDEX_USCPI, MONTH_LAG_TIPS_1, START_DATE_TIPS_1, INDEX_START_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1, NOTIONAL_TIPS_1, REAL_RATE_TIPS_1, BUSINESS_DAY_USD,
          SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD, DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, ISSUER_US_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> US_CPI = MarketDiscountDataSets.usCpiFrom2009();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_TIPS_1 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(PRICING_DATE, US_CPI, "Not used");

  private static final double QUANTITY_TIPS_1 = 654321;
  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtils.getUTCDate(2011, 8, 10);
  private static final double PRICE_TIPS_1 = 1.05;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> BOND_TIPS_1_TRANSACTION_DEFINITION = new BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(
      BOND_SECURITY_TIPS_1_DEFINITION, QUANTITY_TIPS_1, SETTLE_DATE_TIPS_1, PRICE_TIPS_1);
  private static final BondCapitalIndexedTransaction<Coupon> BOND_TIPS_1_TRANSACTION = BOND_TIPS_1_TRANSACTION_DEFINITION.toDerivative(PRICING_DATE, US_CPI, "Not used");

  @Test
  public void presentValueTips1() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TRANSACTION.presentValue(BOND_TIPS_1_TRANSACTION, MARKET);
    final MultipleCurrencyAmount pvSecurity = METHOD_BOND_SECURITY.presentValue(BOND_SECURITY_TIPS_1, MARKET);
    final MultipleCurrencyAmount pvSettlement = BOND_TIPS_1_TRANSACTION.getBondTransaction().getSettlement().accept(PVIC, MARKET).multipliedBy(
        BOND_TIPS_1_TRANSACTION.getQuantity() * BOND_TIPS_1_TRANSACTION.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    assertEquals("Inflation Capital Indexed bond transaction: present value", pvSecurity.multipliedBy(QUANTITY_TIPS_1).plus(pvSettlement).getAmount(BOND_SECURITY_TIPS_1.getCurrency()),
        pv.getAmount(BOND_SECURITY_TIPS_1.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_TRANSACTION.presentValue(BOND_TIPS_1_TRANSACTION, MARKET);
    final MultipleCurrencyAmount pvCalculator = BOND_TIPS_1_TRANSACTION.accept(PVIC, MARKET);
    assertEquals("Inflation Capital Indexed bond transaction: Method vs Calculator", pvMethod, pvCalculator);
  }
}
