/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 * Test related to the construction of Cap/floor on Ibor.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborTest {

  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final double NOTIONAL = 1000000;
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  // The dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final double PAYMENT_YEAR_FRACTION = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FIXING_YEAR_FRACTION = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  // Reference date and time.
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_START_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);

  private static final CapFloorIbor CAP = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
      FIXING_YEAR_FRACTION, STRIKE, IS_CAP);

  @Test
  public void testGetters() {
    assertEquals("Getter strike", STRIKE, CAP.getStrike());
    assertEquals("Getter cap flag", IS_CAP, CAP.isCap());
    final double fixingRate = 0.05;
    assertEquals("Pay-off", Math.max(fixingRate - STRIKE, 0), CAP.payOff(fixingRate));
  }

  @Test
  public void withStrike() {
    final double otherStrike = STRIKE + 0.01;
    final CapFloorIbor otherCap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, otherStrike, IS_CAP);
    final CapFloorIbor otherCapWith = CAP.withStrike(otherStrike);
    assertEquals("Strike", otherStrike, otherCapWith.getStrike());
    assertEquals("Pay-off", otherCap, otherCapWith);
  }

  @Test
  public void withNotional() {
    final double notional = NOTIONAL + 10000;
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, notional, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertEquals(cap, CAP.withNotional(notional));
  }

  @Test
  public void testToCoupon() {

  }

  @Test
  public void testHashCodeEquals() {
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    CapFloorIbor other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertEquals(cap, other);
    assertEquals(cap.hashCode(), other.hashCode());
    other = new CapFloorIbor(Currency.AUD, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME + 1, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION + 1, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL + 1, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME - 1e-8, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    final IborIndex index = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor");
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, index, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME - 1e-8, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME + 1,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION + 1, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE + 1, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, !IS_CAP);
    assertFalse(other.equals(cap));
  }
}
