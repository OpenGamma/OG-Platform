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
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the construction of annuities definition of different types.
 */
public class AnnuityDefinitionBuilderTest {

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M");
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M");
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");

  private static final double NOTIONAL = 10000000; // 10m
  private static final double SPREAD = 0.0010; // 10bps
  private static final double RATE = 0.0100; // 1.00%

  private static final RollDateAdjuster QUARTERLY_IMM_ADJUSTER = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);

  @Test
  public void couponFixedRollDate() {
    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15) };
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2013, 8, 20);
    final int startRoll1 = 1;
    final int endRoll1 = 5; // 1Y swap with 2 semi-annual coupons.
    AnnuityDefinition<CouponFixedDefinition> leg1 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll1, endRoll1, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll1 - startRoll1) / 2, leg1.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg1.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * loopcpn], leg1.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * (loopcpn + 1)], leg1.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg1.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg1.getNthPayment(loopcpn).getRate()); // Payer
    }
    final int startRoll2 = 3;
    final int endRoll2 = 12; // 2Y3M swap with semi-annual coupons.
    // SHORT_START: 1-2-2-2-2
    AnnuityDefinition<CouponFixedDefinition> leg2 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, endRoll2, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, false, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg2.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg2.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg2.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg2.getNthPayment(0).getRate());
    for (int loopcpn = 1; loopcpn < leg2.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * (loopcpn + 1)], leg2.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg2.getNthPayment(loopcpn).getRate());
    }
    // LONG_START: 3-2-2-2
    AnnuityDefinition<CouponFixedDefinition> leg3 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, endRoll2, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg3.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg3.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2], leg3.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg3.getNthPayment(0).getRate());
    for (int loopcpn = 1; loopcpn < leg3.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * loopcpn], leg3.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * (loopcpn + 1)], leg3.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg3.getNthPayment(loopcpn).getRate());
    }
    // SHORT_END: 2-2-2-2-1
    AnnuityDefinition<CouponFixedDefinition> leg4 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, endRoll2, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg4.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg4.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg4.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg4.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg4.getNthPayment(loopcpn).getRate());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 2], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getRate());
    // LONG_END: 2-2-2-3
    AnnuityDefinition<CouponFixedDefinition> leg5 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, endRoll2, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg5.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg5.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg5.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg5.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg5.getNthPayment(loopcpn).getRate());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 4], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getRate());
    // LONG_START: 1
    AnnuityDefinition<CouponFixedDefinition> leg6 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, startRoll2 + 1, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg6.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg6.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg6.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg6.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg6.getNthPayment(0).getRate());
    // LONG_END: 1
    AnnuityDefinition<CouponFixedDefinition> leg7 = AnnuityDefinitionBuilder.couponFixedRollDate(USDLIBOR3M.getCurrency(), referenceDate, startRoll2, startRoll2 + 1, QUARTERLY_IMM_ADJUSTER,
        USDLIBOR6M.getTenor(), NOTIONAL, RATE, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg7.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg7.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg7.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg7.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", RATE, leg7.getNthPayment(0).getRate());
  }

  @Test
  public void couponIborStandard() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 9, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2);
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborDefinition> leg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, paymentPeriod, NOTIONAL,
        USDLIBOR6M, true, USDLIBOR6M.getBusinessDayConvention(), USDLIBOR6M.isEndOfMonth(), USDLIBOR6M.getDayCount(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 9, 22),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
    }
  }

  @Test
  public void couponIborShortFirst() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 8, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2).plusMonths(1); // 2Y 1M
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborDefinition> leg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, paymentPeriod, NOTIONAL,
        USDLIBOR6M, true, USDLIBOR6M.getBusinessDayConvention(), USDLIBOR6M.isEndOfMonth(), USDLIBOR6M.getDayCount(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 20), DateUtils.getUTCDate(2014, 3, 20),
      DateUtils.getUTCDate(2014, 9, 22), DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
    }
  }

  @Test
  public void couponIborRollDate() {
    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15) };
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2013, 8, 20);
    final int startRoll1 = 1;
    final int endRoll1 = 5; // 1Y swap with 4 quarterly coupons.
    AnnuityDefinition<CouponIborDefinition> leg1 = AnnuityDefinitionBuilder.couponIborRollDate(referenceDate, startRoll1, endRoll1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR3M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC);
    assertEquals("AnnuityDefinitionBuilder: annuityIborFromRollDate", endRoll1 - startRoll1, leg1.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg1.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn], leg1.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn], leg1.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn + 1], leg1.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg1.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg1.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR3M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", NOTIONAL, -leg1.getNthPayment(loopcpn).getNotional()); // Payer
    }
    final int startRoll2 = 3;
    final int endRoll2 = 11; // 2Y swap with 8 quarterly coupons.
    AnnuityDefinition<CouponIborDefinition> leg2 = AnnuityDefinitionBuilder.couponIborRollDate(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR3M, NOTIONAL, false, USDLIBOR6M.getDayCount(), NYC);
    assertEquals("AnnuityDefinitionBuilder: annuityIborFromRollDate", endRoll2 - startRoll2, leg2.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg2.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn + 2], leg2.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn + 2], leg2.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[loopcpn + 3], leg2.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg2.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg2.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR3M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", NOTIONAL, leg2.getNthPayment(loopcpn).getNotional()); // Receiver
    }
  }

  @Test
  public void couponIborRollDateIndexAdjusted() {
    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15) };
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2013, 8, 20);
    final int startRoll1 = 1;
    final int endRoll1 = 5; // 1Y swap with 2 semi-annual coupons.
    AnnuityDefinition<CouponIborDefinition> leg1 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll1, endRoll1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll1 - startRoll1) / 2, leg1.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg1.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * loopcpn], leg1.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[2 * loopcpn], leg1.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * (loopcpn + 1)], leg1.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg1.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg1.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg1.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", USDLIBOR6M, leg1.getNthPayment(loopcpn).getIndex()); // Payer
    }
    final int startRoll2 = 3;
    final int endRoll2 = 12; // 2Y3M swap with semi-annual coupons.
    // SHORT_START: 1-2-2-2-2
    AnnuityDefinition<CouponIborDefinition> leg2 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, false, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg2.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg2.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg2.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg2.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg2.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg2.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(0).getNotional());
    for (int loopcpn = 1; loopcpn < leg2.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * (loopcpn + 1)], leg2.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg2.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg2.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(loopcpn).getNotional());
    }
    // LONG_START: 3-2-2-2
    AnnuityDefinition<CouponIborDefinition> leg3 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg3.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg3.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg3.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2], leg3.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg3.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg3.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(0).getNotional());
    for (int loopcpn = 1; loopcpn < leg3.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * loopcpn], leg3.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * (loopcpn + 1)], leg3.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg3.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg3.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(loopcpn).getNotional());
    }
    // SHORT_END: 2-2-2-2-1
    AnnuityDefinition<CouponIborDefinition> leg4 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg4.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg4.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg4.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg4.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg4.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg4.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg4.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(loopcpn).getNotional());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 2], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[endRoll2 - 2], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getNotional());
    // LONG_END: 2-2-2-3
    AnnuityDefinition<CouponIborDefinition> leg5 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg5.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg5.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg5.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg5.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg5.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg5.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg5.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(loopcpn).getNotional());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 4], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[endRoll2 - 4], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getNotional());
    // LONG_START: 1
    AnnuityDefinition<CouponIborDefinition> leg6 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, startRoll2 + 1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg6.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg6.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg6.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg6.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg6.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg6.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg6.getNthPayment(0).getNotional());
    // LONG_END: 1
    AnnuityDefinition<CouponIborDefinition> leg7 = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(referenceDate, startRoll2, startRoll2 + 1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg7.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg7.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg5.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg7.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg7.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg7.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg7.getNthPayment(0).getNotional());
  }

  @Test
  public void couponIborSpreadRollDateIndexAdjusted() {
    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15) };
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2013, 8, 20);
    final int startRoll1 = 1;
    final int endRoll1 = 5; // 1Y swap with 2 semi-annual coupons.
    AnnuityDefinition<CouponIborSpreadDefinition> leg1 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll1, endRoll1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll1 - startRoll1) / 2, leg1.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg1.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * loopcpn], leg1.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[2 * loopcpn], leg1.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[2 * (loopcpn + 1)], leg1.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg1.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg1.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg1.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg1.getNthPayment(loopcpn).getSpread()); // Payer
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", USDLIBOR6M, leg1.getNthPayment(loopcpn).getIndex()); // Payer
    }
    final int startRoll2 = 3;
    final int endRoll2 = 12; // 2Y3M swap with semi-annual coupons.
    // SHORT_START: 1-2-2-2-2
    AnnuityDefinition<CouponIborSpreadDefinition> leg2 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, false, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg2.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg2.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg2.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg2.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg2.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg2.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg2.getNthPayment(0).getSpread()); // Payer
    for (int loopcpn = 1; loopcpn < leg2.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 2 + 2 * (loopcpn + 1)], leg2.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg2.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg2.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, leg2.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg2.getNthPayment(loopcpn).getSpread()); // Payer
    }
    // LONG_START: 3-2-2-2
    AnnuityDefinition<CouponIborSpreadDefinition> leg3 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg3.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg3.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg3.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2], leg3.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg3.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg3.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg3.getNthPayment(0).getSpread()); // Payer
    for (int loopcpn = 1; loopcpn < leg3.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * loopcpn], leg3.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 2 + 2 * loopcpn], leg2.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 + 2 * (loopcpn + 1)], leg3.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg3.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg3.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg3.getNthPayment(loopcpn).getNotional());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg3.getNthPayment(loopcpn).getSpread()); // Payer
    }
    // SHORT_END: 2-2-2-2-1
    AnnuityDefinition<CouponIborSpreadDefinition> leg4 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.SHORT_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2 + 1, leg4.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg4.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg4.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg4.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg4.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg4.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg4.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(loopcpn).getNotional());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 2], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[endRoll2 - 2], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg4.getNthPayment(leg4.getNumberOfPayments() - 1).getNotional());
    // LONG_END: 2-2-2-3
    AnnuityDefinition<CouponIborSpreadDefinition> leg5 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, endRoll2,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", (endRoll2 - startRoll2) / 2, leg5.getNumberOfPayments()); // Number of coupons
    for (int loopcpn = 0; loopcpn < leg5.getNumberOfPayments() - 1; loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg5.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1 + 2 * loopcpn], leg5.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1 + 2 * (loopcpn + 1)], leg5.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg5.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg5.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(loopcpn).getNotional());
    }
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 4], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[endRoll2 - 4], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[endRoll2 - 1], leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg5.getNthPayment(leg5.getNumberOfPayments() - 1).getNotional());
    // LONG_START: 1
    AnnuityDefinition<CouponIborSpreadDefinition> leg6 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, startRoll2 + 1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_START);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg6.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg6.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg6.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg6.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg6.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg6.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg6.getNthPayment(0).getNotional());
    // LONG_END: 1
    AnnuityDefinition<CouponIborSpreadDefinition> leg7 = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(referenceDate, startRoll2, startRoll2 + 1,
        QUARTERLY_IMM_ADJUSTER, USDLIBOR6M, SPREAD, NOTIONAL, true, USDLIBOR6M.getDayCount(), NYC, StubType.LONG_END);
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", 1, leg7.getNumberOfPayments()); // Number of coupons
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2 - 1], leg7.getNthPayment(0).getAccrualStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", expectedDates[startRoll2 - 1], leg5.getNthPayment(0).getFixingPeriodStartDate());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", expectedDates[startRoll2], leg7.getNthPayment(0).getAccrualEndDate());
    assertEquals("AnnuityDefinitionBuilder: couponIborRollDate", leg7.getNthPayment(0).getFixingPeriodEndDate(),
        ScheduleCalculator.getAdjustedDate(leg7.getNthPayment(0).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", NOTIONAL, -leg7.getNthPayment(0).getNotional());
    assertEquals("AnnuityDefinitionBuilder: couponFixedRollDate", SPREAD, leg7.getNthPayment(0).getSpread()); // Payer
  }

  @Test
  public void couponIborSpreadStandard() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 9, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2);
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborSpreadDefinition> leg = AnnuityDefinitionBuilder.couponIborSpread(settlementDate, maturityDate, paymentPeriod, NOTIONAL, SPREAD,
        USDLIBOR6M, true, USDLIBOR6M.getBusinessDayConvention(), USDLIBOR6M.isEndOfMonth(), USDLIBOR6M.getDayCount(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 9, 22),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getFixingPeriodEndDate(),
          ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingPeriodStartDate(), USDLIBOR6M, NYC));
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", SPREAD, leg.getNthPayment(loopcpn).getSpread());
    }
  }

  @Test
  public void couponIborCompoundingStandard() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 9, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2);
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborCompoundingDefinition> leg = AnnuityDefinitionBuilder.couponIborCompounding(settlementDate, maturityDate, paymentPeriod, NOTIONAL,
        USDLIBOR3M, stub, true, USDLIBOR3M.getBusinessDayConvention(), USDLIBOR3M.isEndOfMonth(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 9, 22),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingPeriodStartDates()[0]);
      for (int loopsub = 0; loopsub < leg.getNthPayment(loopcpn).getAccrualEndDates().length; loopsub++) {
        assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", leg.getNthPayment(loopcpn).getFixingPeriodEndDates()[loopsub],
            ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingPeriodStartDates()[loopsub], USDLIBOR3M, NYC));
      }
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Compounding", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
    }
  }

  @Test
  public void couponIborCompoundingSpreadStandard() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 9, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2);
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborCompoundingSpreadDefinition> leg = AnnuityDefinitionBuilder.couponIborCompoundingSpread(settlementDate, maturityDate, paymentPeriod, NOTIONAL, SPREAD,
        USDLIBOR3M, stub, true, USDLIBOR3M.getBusinessDayConvention(), USDLIBOR3M.isEndOfMonth(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 9, 22),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingPeriodStartDates()[0]);
      for (int loopsub = 0; loopsub < leg.getNthPayment(loopcpn).getAccrualEndDates().length; loopsub++) {
        assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getFixingPeriodEndDates()[loopsub],
            ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingPeriodStartDates()[loopsub], USDLIBOR3M, NYC));
      }
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", SPREAD, leg.getNthPayment(loopcpn).getSpread());
    }
  }

  @Test
  public void couponIborCompoundingFlatSpreadStandard() {
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 9, 20);
    ZonedDateTime maturityDate = settlementDate.plusYears(2);
    Period paymentPeriod = Period.ofMonths(6);
    final StubType stub = StubType.SHORT_START;
    AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> leg = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(settlementDate, maturityDate, paymentPeriod, NOTIONAL, SPREAD,
        USDLIBOR3M, stub, true, USDLIBOR3M.getBusinessDayConvention(), USDLIBOR3M.isEndOfMonth(), NYC, stub);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 9, 22),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2015, 9, 21) };
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates.length, leg.getNumberOfPayments());
    assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", settlementDate, leg.getNthPayment(0).getAccrualStartDate());
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", expectedPaymentDates[loopcpn], leg.getNthPayment(loopcpn).getPaymentDate());
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getAccrualStartDate(), leg.getNthPayment(loopcpn).getFixingSubperiodStartDates()[0]);
      for (int loopsub = 0; loopsub < leg.getNthPayment(loopcpn).getSubperiodsAccrualStartDates().length; loopsub++) {
        assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", leg.getNthPayment(loopcpn).getFixingSubperiodEndDates()[loopsub],
            ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopcpn).getFixingSubperiodStartDates()[loopsub], USDLIBOR3M, NYC));
      }
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", NOTIONAL, -leg.getNthPayment(loopcpn).getNotional()); // Payer
      assertEquals("AnnuityDefinitionBuilder: Coupon Ibor Spread", SPREAD, leg.getNthPayment(loopcpn).getSpread());
    }
  }

}
