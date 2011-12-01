/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

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
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of CMS legs.
 */
public class AnnuityCouponCMSDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // Ibor index
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  //CMS 10Y
  private static final Period CMS_TENOR = Period.ofYears(10);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, CMS_TENOR);
  // Annuity
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime MATURITY_DATE = START_DATE.plus(ANNUITY_TENOR);
  private static final double NOTIONAL = 100000000; //100m
  private static final Period LEG_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount LEG_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponCMSDefinition CMS_LEG = AnnuityCouponCMSDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX, LEG_PAYMENT_PERIOD, LEG_DAY_COUNT, IS_PAYER);

  @Test
  public void dates() {
    IborIndex fakeIborIndex12 = new IborIndex(CUR, LEG_PAYMENT_PERIOD, IBOR_SETTLEMENT_DAYS, CALENDAR, LEG_DAY_COUNT, BUSINESS_DAY, IS_EOM);
    AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, fakeIborIndex12, IS_PAYER);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualStartDate(), CMS_LEG.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualEndDate(), CMS_LEG.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentYearFraction(), CMS_LEG.getNthPayment(loopcpn).getPaymentYearFraction());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentDate(), CMS_LEG.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getFixingDate(), CMS_LEG.getNthPayment(loopcpn).getFixingDate());
    }
  }

  @Test
  public void index() {
    for (int loopcpn = 0; loopcpn < CMS_LEG.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX, CMS_LEG.getNthPayment(loopcpn).getCMSIndex());
      assertEquals(NOTIONAL * (IS_PAYER ? -1.0 : 1.0), CMS_LEG.getNthPayment(loopcpn).getNotional());
    }
    AnnuityCouponCMSDefinition cmsLegReceive = AnnuityCouponCMSDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX, LEG_PAYMENT_PERIOD, LEG_DAY_COUNT, !IS_PAYER);
    for (int loopcpn = 0; loopcpn < CMS_LEG.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX, cmsLegReceive.getNthPayment(loopcpn).getCMSIndex());
      assertEquals(-NOTIONAL * (IS_PAYER ? -1.0 : 1.0), cmsLegReceive.getNthPayment(loopcpn).getNotional());
    }
  }

}
