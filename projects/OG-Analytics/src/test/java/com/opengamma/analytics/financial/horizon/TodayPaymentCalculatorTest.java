/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests functionality of TPC, which returns currency amounts paid on provided date
 */
@Test(groups = TestGroup.UNIT)
public class TodayPaymentCalculatorTest {

  // Swap Fixed-Ibor
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD Calendar");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR_USD);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);
  // Market
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_3 = ImmutableZonedDateTimeDoubleTimeSeries.of(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15)},
      new double[] {0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160}, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_6 = ImmutableZonedDateTimeDoubleTimeSeries.of(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16)}, new double[] {0.0095, 0.0120, 0.0130}, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6};

  // Tests
  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m

  @Test
  public void todayPaymentCalculatorOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOnDayBeforePayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 16);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = 0.0;

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOnDayAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 18);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = 0.0;

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOverWeekIncludingPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 15);
    final ZonedDateTime horizonDate = referenceDate.plusDays(7);

    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(TimeCalculator.getTimeBetween(referenceDate, horizonDate));
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  // BACKWARD LOOKING TESTS //////////////////////////////////////////
  @Test
  public void tpcLookingBackwardOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.minusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void tpcLookingBackwardOneDayOneDayAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 18);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double todayCash = 0.0; // ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.minusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void tpcLookingBackwardAndForwardOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);

    final ZonedDateTime forwardHorizonDate = referenceDate.minusDays(1);
    final double forwardHorizon = TimeCalculator.getTimeBetween(referenceDate, forwardHorizonDate); // !!! Negative horizon
    final TodayPaymentCalculator forwardCalculator = TodayPaymentCalculator.getInstance(forwardHorizon);
    final MultipleCurrencyAmount paymentIfLookingForward = swapToday.accept(forwardCalculator);

    final ZonedDateTime backwardHorizonDate = referenceDate.minusDays(1);
    final double backwardHorizon = TimeCalculator.getTimeBetween(referenceDate, backwardHorizonDate); // !!! Negative horizon
    final TodayPaymentCalculator backwardCalculator = TodayPaymentCalculator.getInstance(backwardHorizon);
    final MultipleCurrencyAmount paymentIfLookingBackward = swapToday.accept(backwardCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", paymentIfLookingForward.getAmount(USD6MLIBOR3M.getCurrency()), paymentIfLookingBackward.getAmount(USD6MLIBOR3M.getCurrency()),
        TOLERANCE_PV);
  }

  @Test
  // The following test fails because the payment on 2012/08/17 is dropped when toDerivative is called.
  // TodayPaymentCalculator does what it says on the tin. It provides the cashflows that occur today.
  // The horizon is there only to give flexibility in financial-time as to the range in which one considers something as having occurred today.
  public void tpcWontProvidePaymentFromOneWeekBack() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 21);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final ZonedDateTime horizonDate = referenceDate.minusDays(7);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }
}
