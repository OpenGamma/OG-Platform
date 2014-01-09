/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of CMS spread cap/floor.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCapFloorCMSSpreadDefinitionTest {
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // Ibor index
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  // CMS Index
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  //CMS 10Y
  private static final Period CMS_TENOR_10 = Period.ofYears(10);
  private static final IndexSwap CMS_INDEX_10 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, CMS_TENOR_10, CALENDAR);
  //CMS 2Y
  private static final Period CMS_TENOR_2 = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX_2 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, CMS_TENOR_2, CALENDAR);
  // Annuity
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime MATURITY_DATE = START_DATE.plus(ANNUITY_TENOR);
  private static final double NOTIONAL = 100000000; //100m
  private static final Period LEG_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount LEG_DAY_COUNT = DayCounts.ACT_365;
  private static final boolean IS_PAYER = true;
  private static final double STRIKE = 0.0050;
  private static final boolean IS_CAP = true;
  private static final AnnuityCapFloorCMSSpreadDefinition CMS_SPREAD_LEG = AnnuityCapFloorCMSSpreadDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX_10, CMS_INDEX_2, LEG_PAYMENT_PERIOD,
      LEG_DAY_COUNT, IS_PAYER, STRIKE, IS_CAP, CALENDAR, CALENDAR);

  @Test
  public void dates() {
    final IborIndex fakeIborIndex12 = new IborIndex(CUR, LEG_PAYMENT_PERIOD, IBOR_SETTLEMENT_DAYS, LEG_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, fakeIborIndex12, IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualStartDate(), CMS_SPREAD_LEG.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualEndDate(), CMS_SPREAD_LEG.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentYearFraction(), CMS_SPREAD_LEG.getNthPayment(loopcpn).getPaymentYearFraction());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentDate(), CMS_SPREAD_LEG.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getFixingDate(), CMS_SPREAD_LEG.getNthPayment(loopcpn).getFixingDate());
    }
  }

  @Test
  public void common() {
    for (int loopcpn = 0; loopcpn < CMS_SPREAD_LEG.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX_10, CMS_SPREAD_LEG.getNthPayment(loopcpn).getCmsIndex1());
      assertEquals(CMS_INDEX_2, CMS_SPREAD_LEG.getNthPayment(loopcpn).getCmsIndex2());
      assertEquals(NOTIONAL * (IS_PAYER ? -1.0 : 1.0), CMS_SPREAD_LEG.getNthPayment(loopcpn).getNotional());
      assertEquals(STRIKE, CMS_SPREAD_LEG.getNthPayment(loopcpn).getStrike());
      assertEquals(IS_CAP, CMS_SPREAD_LEG.getNthPayment(loopcpn).isCap());
    }
    final AnnuityCapFloorCMSSpreadDefinition cmsCapReceiver = AnnuityCapFloorCMSSpreadDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX_10, CMS_INDEX_2, LEG_PAYMENT_PERIOD, LEG_DAY_COUNT,
        !IS_PAYER, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    for (int loopcpn = 0; loopcpn < cmsCapReceiver.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX_10, cmsCapReceiver.getNthPayment(loopcpn).getCmsIndex1());
      assertEquals(CMS_INDEX_2, cmsCapReceiver.getNthPayment(loopcpn).getCmsIndex2());
      assertEquals(-NOTIONAL * (IS_PAYER ? -1.0 : 1.0), cmsCapReceiver.getNthPayment(loopcpn).getNotional());
    }
  }
}
