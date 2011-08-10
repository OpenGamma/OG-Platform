/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationGearingDiscountingMethod;
import com.opengamma.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Tests the present value of Capital inflation indexed bonds.
 */
public class BondCapitalIndexedSecurityDiscountingMethodTest {
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final PriceIndex[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new PriceIndex[0]);
  private static final PriceIndex PRICE_INDEX_USCPI = PRICE_INDEXES[0];
  private static final PriceIndex PRICE_INDEX_UKRPI = PRICE_INDEXES[2];
  private static final String[] ISSUER_NAMES = MARKET.getIssuers().toArray(new String[0]);
  private static final String ISSUER_UK_GOVT = ISSUER_NAMES[0];
  private static final String ISSUER_US_GOVT = ISSUER_NAMES[1];

  private static final ZonedDateTime PRICING_DATE = DateUtil.getUTCDate(2011, 8, 8);
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_INFLATION = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD_INFLATION_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD_INFLATION_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_GILT_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE_GILT_1 = DateUtil.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE_GILT_1 = DateUtil.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE_GILT_1 = DateUtil.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_GILT_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_GILT_1 = 8;
  private static final double INDEX_START_GILT_1 = 173.60; // November 2001 
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final double REAL_RATE_GILT_1 = 0.02;
  private static final Period COUPON_PERIOD_GILT_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  // TODO: ex-coupon 7 days
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_GILT_1_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MarketDataSets.ukRpiFrom2010();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_GILT_1 = BOND_SECURITY_GILT_1_DEFINITION.toDerivative(PRICING_DATE, UK_RPI, "Not used");

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueGilt1() {
    MarketBundle marketUKGovt = new MarketBundle();
    marketUKGovt.setCurve(BOND_SECURITY_GILT_1.getCurrency(), MARKET.getCurve(ISSUER_UK_GOVT));
    marketUKGovt.setCurve(PRICE_INDEX_UKRPI, MARKET.getCurve(PRICE_INDEX_UKRPI));
    CurrencyAmount pvNominal = METHOD_INFLATION_ZC_MONTHLY.presentValue(BOND_SECURITY_GILT_1.getNominal().getNthPayment(0), marketUKGovt);
    CurrencyAmount pvCoupon = CurrencyAmount.of(BOND_SECURITY_GILT_1.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY_GILT_1.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(PVIC.visit(BOND_SECURITY_GILT_1.getCoupon().getNthPayment(loopcpn), marketUKGovt));
    }
    CurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getAmount(), pv.getAmount(), 1.0E-2);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getCurrency(), pv.getCurrency());
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    CurrencyAmount pvCalculator = PVIC.visit(BOND_SECURITY_GILT_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvMethod, pvCalculator);
  }

  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount DAY_COUNT_TIPS_1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final boolean IS_EOM_TIPS_1 = false;
  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtil.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtil.getUTCDate(2016, 1, 15);
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
  private static final DoubleTimeSeries<ZonedDateTime> US_CPI = MarketDataSets.usCpiFrom2009();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_TIPS_1 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(PRICING_DATE, US_CPI, "Not used");

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueTips1() {
    MarketBundle marketUSGovt = new MarketBundle();
    marketUSGovt.setCurve(BOND_SECURITY_TIPS_1.getCurrency(), MARKET.getCurve(ISSUER_US_GOVT));
    marketUSGovt.setCurve(PRICE_INDEX_USCPI, MARKET.getCurve(PRICE_INDEX_USCPI));
    CurrencyAmount pvNominal = METHOD_INFLATION_ZC_INTERPOLATION.presentValue(BOND_SECURITY_TIPS_1.getNominal().getNthPayment(0), marketUSGovt);
    CurrencyAmount pvCoupon = CurrencyAmount.of(BOND_SECURITY_TIPS_1.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY_TIPS_1.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(PVIC.visit(BOND_SECURITY_TIPS_1.getCoupon().getNthPayment(loopcpn), marketUSGovt));
    }
    CurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_TIPS_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getAmount(), pv.getAmount(), 1.0E-2);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getCurrency(), pv.getCurrency());
  }

}
