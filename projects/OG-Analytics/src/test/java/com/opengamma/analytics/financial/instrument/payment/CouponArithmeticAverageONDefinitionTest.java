/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

public class CouponArithmeticAverageONDefinitionTest {

  private static final BusinessDayConvention MOD_FOL = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 4, 18);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final int PAYMENT_LAG = 2;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);
  private static final CouponArithmeticAverageONDefinition FEDFUND_CPN_3M_DEF = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
      MOD_FOL, true, NYC);
  private static final CouponArithmeticAverageONDefinition FEDFUND_CPN_3M_2_DEF = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 4, 16);
  private static final String[] NOT_USED = new String[] {"Not used 1", "not used 2" };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    CouponArithmeticAverageONDefinition.from(null, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartDate() {
    CouponArithmeticAverageONDefinition.from(FEDFUND, null, TENOR_3M, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, null, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBD() {
    CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, 0, null, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate() {
    CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, null, NOTIONAL, PAYMENT_LAG, NYC);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getPaymentDate(), PAYMENT_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getAccrualStartDate(), EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodDate()[0], EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodDate()[FEDFUND_CPN_3M_DEF.getFixingPeriodDate().length - 1], ACCRUAL_END_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getCurrency(), FEDFUND.getCurrency());
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getIndex(), FEDFUND);
  }

  @Test
  public void from() {
    assertEquals("CouponArithmeticAverageON: from", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_2_DEF);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_DEF);
    final CouponArithmeticAverageONDefinition other = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
        MOD_FOL, true, NYC);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, other);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.hashCode(), other.hashCode());
    CouponArithmeticAverageONDefinition modified;
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponArithmeticAverageONDefinition.from(modifiedIndex, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG, MOD_FOL, true, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE.plusDays(1), ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE.plusDays(1), NOTIONAL, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE, NOTIONAL + 1000, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponArithmeticAverageONDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG + 1, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
  }

  @Test
  public void toDerivativesNoData() {
    final CouponArithmeticAverageON cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(REFERENCE_DATE, NOT_USED);
    final double payTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_DEF.getPaymentDate());
    final double[] fixingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_DEF.getFixingPeriodDate());
    final CouponArithmeticAverageON cpnExpected = CouponArithmeticAverageON.from(payTime, FEDFUND_CPN_3M_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, fixingTime,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactor(), 0);
    assertEquals("CouponArithmeticAverageONDefinition: toDerivative", cpnExpected, cpnConverted);
  }

}
