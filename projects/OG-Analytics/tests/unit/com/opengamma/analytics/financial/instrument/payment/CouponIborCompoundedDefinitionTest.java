/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the building of compounded Ibor coupons.
 */
public class CouponIborCompoundedDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborTestsMaster MASTER_IBOR = IndexIborTestsMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M", NYC);

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;

  private static final CouponIborCompoundedDefinition CPN_FROM_INDEX = CouponIborCompoundedDefinition.from(NOTIONAL, START_DATE, TENOR_3M, USDLIBOR1M);

  @Test
  public void from() {
    int nbSubPeriodExpected = 3;
    ZonedDateTime[] endAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M);
    ZonedDateTime[] startAccrualDates = new ZonedDateTime[nbSubPeriodExpected];
    double[] paymentAccrualFactors = new double[nbSubPeriodExpected];
    startAccrualDates[0] = START_DATE;
    for (int loopsub = 1; loopsub < nbSubPeriodExpected; loopsub++) {
      startAccrualDates[loopsub] = endAccrualDates[loopsub - 1];
    }
    for (int loopsub = 0; loopsub < nbSubPeriodExpected; loopsub++) {
      paymentAccrualFactors[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(startAccrualDates[loopsub], endAccrualDates[loopsub]);
    }
    CouponIborCompoundedDefinition cpnFromAccrualDates = CouponIborCompoundedDefinition.from(endAccrualDates[nbSubPeriodExpected - 1], NOTIONAL, USDLIBOR1M, startAccrualDates, endAccrualDates,
        paymentAccrualFactors);
    assertEquals("CouponIborCompoundedDefinition: from", cpnFromAccrualDates, CPN_FROM_INDEX);
    assertArrayEquals("CouponIborCompoundedDefinition: getter", startAccrualDates, CPN_FROM_INDEX.getAccrualStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", startAccrualDates, CPN_FROM_INDEX.getFixingPeriodStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", endAccrualDates, CPN_FROM_INDEX.getAccrualEndDates());
    ZonedDateTime[] fixingDates = ScheduleCalculator.getAdjustedDate(startAccrualDates, -USDLIBOR1M.getSpotLag(), NYC);
    assertArrayEquals("CouponIborCompoundedDefinition: getter", fixingDates, CPN_FROM_INDEX.getFixingDates());
    ZonedDateTime[] fixingPeriodEndDates = ScheduleCalculator.getAdjustedDate(startAccrualDates, USDLIBOR1M);
    assertArrayEquals("CouponIborCompoundedDefinition: getter", fixingPeriodEndDates, CPN_FROM_INDEX.getFixingPeriodEndDates());
  }

  @Test
  public void getter() {
    assertEquals("CouponIborCompoundedDefinition: getter", USDLIBOR1M, CPN_FROM_INDEX.getIndex());
    assertEquals("CouponIborCompoundedDefinition: getter", START_DATE, CPN_FROM_INDEX.getAccrualStartDate());
    assertEquals("CouponIborCompoundedDefinition: getter", START_DATE, CPN_FROM_INDEX.getAccrualStartDates()[0]);
    assertEquals("CouponIborCompoundedDefinition: getter", CPN_FROM_INDEX.getPaymentDate(), CPN_FROM_INDEX.getAccrualEndDates()[CPN_FROM_INDEX.getAccrualEndDates().length - 1]);

  }
}
