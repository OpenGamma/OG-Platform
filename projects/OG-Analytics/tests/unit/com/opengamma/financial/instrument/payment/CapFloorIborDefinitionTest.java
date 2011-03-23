package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

public class CapFloorIborDefinitionTest {
  private static final Tenor TENOR = new Tenor(Period.ofMonths(3));
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, TENOR);

  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double ACCRUAL_FACTOR_FIXING = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final double STRIKE = 0.02;
  private static final boolean IS_CAP = true;

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative

  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON_2 = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);

  private static final CapFloorIborDefinition IBOR_CAP = CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);

  private static final CapFloorIborDefinition IBOR_CAP_2 = CapFloorIborDefinition.from(IBOR_COUPON_2, STRIKE, IS_CAP);

  @Test
  public void test() {
    assertEquals(IBOR_CAP.getPaymentDate(), PAYMENT_DATE);
    assertEquals(IBOR_CAP.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(IBOR_CAP.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(IBOR_CAP.getPaymentYearFraction(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(IBOR_CAP.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_CAP.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_CAP.isFixed(), false);
    assertEquals(IBOR_CAP.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertEquals(IBOR_CAP_2.getPaymentDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getAccrualStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP_2.getAccrualEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getPaymentYearFraction(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertEquals(IBOR_CAP_2.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_CAP_2.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_CAP_2.isFixed(), false);
    assertEquals(IBOR_CAP_2.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP_2.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CapFloorIborDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, INDEX, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullCoupn() {
    CapFloorIborDefinition.from(null, STRIKE, IS_CAP);
  }

  @Test
  public void testFixingProcess() {
    CapFloorIborDefinition couponWithReset = CapFloorIborDefinition.from(IBOR_COUPON_2, STRIKE, IS_CAP);
    double fixingRate = 0.04;
    assertEquals(couponWithReset.isFixed(), false);
    couponWithReset.fixingProcess(fixingRate);
    assertEquals(couponWithReset.isFixed(), true);
    assertEquals(couponWithReset.getFixedRate(), fixingRate, 1E-10);
    assertEquals(couponWithReset.payOff(fixingRate), fixingRate - STRIKE, 1E-10);
    double fixingRateLow = 0.01;
    assertEquals(couponWithReset.payOff(fixingRateLow), 0.0, 1E-10);
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, IBOR_CAP.getFixindPeriodStartDate());
    double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, IBOR_CAP.getFixindPeriodEndDate());
    String fundingCurve = "Funding";
    String forwardCurve = "Forward";
    String[] curves = {fundingCurve, forwardCurve};
    CapFloorIbor expectedCapIbor = new CapFloorIbor(paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, ACCRUAL_FACTOR_FIXING, forwardCurve,
        STRIKE, IS_CAP);
    CapFloorIbor convertedCapIborDefinition = (CapFloorIbor) IBOR_CAP.toDerivative(REFERENCE_DATE, curves);
    assertEquals(expectedCapIbor, convertedCapIborDefinition);
    CapFloorIborDefinition fixedIborCap = CapFloorIborDefinition.from(IBOR_COUPON_2, STRIKE, IS_CAP);
    double paymentTimeFixed = actAct.getDayCountFraction(zonedDate, IBOR_COUPON_2.getPaymentDate());
    double fixingRate = 0.04;
    fixedIborCap.fixingProcess(fixingRate);
    CouponFixed expectedFixedCoupon = new CouponFixed(paymentTimeFixed, fundingCurve, IBOR_COUPON_2.getPaymentYearFraction(), NOTIONAL, fixingRate - STRIKE);
    CouponFixed convertedCapIborFixed = (CouponFixed) fixedIborCap.toDerivative(REFERENCE_DATE, curves);
    assertEquals(expectedFixedCoupon, convertedCapIborFixed);

  }
}
