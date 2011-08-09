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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponFirstOfMonthDefinition;
import com.opengamma.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.inflation.method.CouponInflationZeroCouponFirstOfMonthDiscountingMethod;
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
  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final PriceIndex[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new PriceIndex[0]);
  private static final PriceIndex PRICE_INDEX_GBP = PRICE_INDEXES[1];
  private static final String[] ISSUER_NAMES = MARKET.getIssuers().toArray(new String[0]);
  private static final String ISSUER_UK_GOVT = ISSUER_NAMES[0];
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
  private static final ZonedDateTime PRICING_DATE = DateUtil.getUTCDate(2011, 8, 8);
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> BOND_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(
      PRICE_INDEX_GBP, MONTH_LAG, START_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, REAL_RATE, BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ISSUER_UK_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MarketDataSets.ukRpiFrom2010();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY = BOND_SECURITY_DEFINITION.toDerivative(PRICING_DATE, UK_RPI, "Not used");
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_INFLATION = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final CouponInflationZeroCouponFirstOfMonthDiscountingMethod METHOD_INFLATION_ZC_MONTHLY = new CouponInflationZeroCouponFirstOfMonthDiscountingMethod();
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    MarketBundle marketUKGovt = new MarketBundle();
    marketUKGovt.setCurve(BOND_SECURITY.getCurrency(), MARKET.getCurve(ISSUER_UK_GOVT));
    marketUKGovt.setCurve(PRICE_INDEX_GBP, MARKET.getCurve(PRICE_INDEX_GBP));
    CurrencyAmount pvNominal = METHOD_INFLATION_ZC_MONTHLY.presentValue(BOND_SECURITY.getNominal().getNthPayment(0), marketUKGovt);
    CurrencyAmount pvCoupon = CurrencyAmount.of(BOND_SECURITY.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(PVIC.visit(BOND_SECURITY.getCoupon().getNthPayment(loopcpn), marketUKGovt));
    }
    CurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    CurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getAmount(), pv.getAmount(), 1.0E-2);
    assertEquals("Inflation Capital Indexed bond: present value", pvExpectd.getCurrency(), pv.getCurrency());
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY, MARKET);
    CurrencyAmount pvCalculator = PVIC.visit(BOND_SECURITY, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvMethod, pvCalculator);
  }

  // TODO: TIPS

}
