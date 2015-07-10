/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests on the constructor of annuity for overnight-indexed coupons with spread.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponONSpreadSimplifiedDefinitionTest {

  private static final IndexON EONIA = IndexONMaster.getInstance().getIndex("EONIA");
  private static final Period PAYMENT_PERIOD = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 2, 1);
  private static final ZonedDateTime END_FIXING_DATE = DateUtils.getUTCDate(2022, 2, 1);
  private static final Period MATURITY_TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000000;
  private static final double SPREAD = 0.0012;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final GeneratorSwapFixedON GENERATOR = new GeneratorSwapFixedON("OIS", EONIA, PAYMENT_PERIOD, DAY_COUNT, BUSINESS_DAY, IS_EOM, 2, CALENDAR);
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponONSpreadSimplifiedDefinition DEFINITION = AnnuityCouponONSpreadSimplifiedDefinition.from(SETTLEMENT_DATE, END_FIXING_DATE, NOTIONAL, SPREAD, GENERATOR, IS_PAYER);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2012, 3, 15);

  static {
    ZonedDateTime date = SETTLEMENT_DATE;
    final List<ZonedDateTime> dates = new ArrayList<>();
    final List<Double> data = new ArrayList<>();
    while (date.isBefore(DATE) || date.equals(DATE)) {
      dates.add(date);
      data.add(Math.random() / 100);
      date = date.plusDays(1);
      if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
        date = date.plusDays(2);
      }
    }
    FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(dates, data, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupons() {
    new AnnuityCouponONSimplifiedDefinition(null, GENERATOR.getIndex(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate1() {
    AnnuityCouponONSimplifiedDefinition.from(null, MATURITY_TENOR, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentFrequency() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, (Period) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator1() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_TENOR, NOTIONAL, null, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate2() {
    AnnuityCouponONSimplifiedDefinition.from(null, END_FIXING_DATE, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, (ZonedDateTime) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator2() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, END_FIXING_DATE, NOTIONAL, null, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoIndexTS() {
    DEFINITION.toDerivative(DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, FIXING_TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexTS() {
    DEFINITION.toDerivative(DATE, (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void from() {
    final AnnuityCouponONSpreadSimplifiedDefinition annuity = AnnuityCouponONSpreadSimplifiedDefinition.from(SETTLEMENT_DATE, END_FIXING_DATE, NOTIONAL, SPREAD, IS_PAYER, PAYMENT_PERIOD, EONIA, 2, BUSINESS_DAY,
        IS_EOM, CALENDAR);
    final int nbCouponComputed = annuity.getNumberOfPayments();
    final int nbCouponExpected = 20; // 10 year * semi-annual
    assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", nbCouponExpected, nbCouponComputed);
    assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", SETTLEMENT_DATE, annuity.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", SETTLEMENT_DATE, annuity.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", END_FIXING_DATE, annuity.getNthPayment(nbCouponComputed - 1).getFixingPeriodEndDate());
    assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", END_FIXING_DATE, annuity.getNthPayment(nbCouponComputed - 1).getAccrualEndDate());
    for (int loopcpn = 0; loopcpn < nbCouponComputed; loopcpn++) {
      assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", NOTIONAL * (IS_PAYER ? -1.0 : 1.0), annuity.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityCouponONSpreadSimplifiedDefinition: from", SPREAD, annuity.getNthPayment(loopcpn).getSpread());
    }
  }

}
