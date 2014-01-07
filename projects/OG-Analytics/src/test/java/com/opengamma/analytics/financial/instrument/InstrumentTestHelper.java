/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AbstractDayCount;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class InstrumentTestHelper {
  public static final Calendar NO_HOLIDAY = new NoHolidayCalendar();
  public static final DayCount SEMI_ANNUAL_DAY_COUNT = new SemiAnnualDayCount();
  public static final DayCount QUARTERLY_DAY_COUNT = new QuarterlyDayCount();
  public static final BusinessDayConvention NONE = BusinessDayConventions.NONE;
  public static final Currency FIXED_INCOME_CURRENCY = Currency.EUR;
  public static final IborIndex USD_IBOR_INDEX1 = new IborIndex(FIXED_INCOME_CURRENCY, Period.ofMonths(6), 0, SEMI_ANNUAL_DAY_COUNT, NONE, false,
      "f");
  public static final IborIndex USD_IBOR_INDEX2 = new IborIndex(FIXED_INCOME_CURRENCY, Period.ofMonths(3), 0, QUARTERLY_DAY_COUNT, NONE, false, "f");
  public static final ZonedDateTime CASH_START = ZonedDateTime.of(LocalDateTime.of(2012, 6, 1, 11, 0, 0, 0), ZoneOffset.UTC);
  public static final ZonedDateTime CASH_MATURITY = ZonedDateTime.of(LocalDateTime.of(2012, 12, 1, 11, 0, 0, 0), ZoneOffset.UTC);
  public static final double CASH_NOTIONAL = 234000;
  public static final double CASH_RATE = 0.002;
  public static final ZonedDateTime PAYMENT_MATURITY = zdt(2011, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final double PAYMENT_AMOUNT = 34500;
  public static final ZonedDateTime FIXED_COUPON_START = zdt(2011, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final ZonedDateTime FIXED_COUPON_MATURITY = zdt(2011, 2, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final double FIXED_COUPON_NOTIONAL = 45600;
  public static final double FIXED_COUPON_RATE = 0.0001;
  public static final double IBOR_COUPON_SPREAD = 0.00023;
  public static final ZonedDateTime FRA_START = zdt(2011, 6, 3, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final ZonedDateTime FRA_END = zdt(2011, 12, 3, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final double FRA_NOTIONAL = 567000;
  public static final double FRA_RATE = 0.004;
  public static final ZonedDateTime SWAP_START = zdt(2001, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final ZonedDateTime SWAP_MATURITY = zdt(2031, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final GeneratorSwapFixedIbor SWAP_GENERATOR = new GeneratorSwapFixedIbor("a", Period.ofMonths(6), SEMI_ANNUAL_DAY_COUNT, USD_IBOR_INDEX1, NO_HOLIDAY);
  public static final double SWAP_NOTIONAL = 789000;
  public static final double SWAP_FIXED_RATE = 0.04;
  public static final double IBOR_SPREAD = 0.01;
  public static final Currency FX_PAY_CURRENCY = Currency.GBP;
  public static final Currency FX_RECEIVE_CURRENCY = Currency.EUR;
  public static final ZonedDateTime FX_MATURITY = DateUtils.getUTCDate(2013, 1, 1);
  public static final double FX_PAY_AMOUNT = -12345;
  public static final double FX_RECEIVE_AMOUNT = 23456;
  public static final GeneratorSwapIborIbor IBOR_IBOR_GENERATOR = new GeneratorSwapIborIbor("s", USD_IBOR_INDEX1, USD_IBOR_INDEX2, NO_HOLIDAY, NO_HOLIDAY);
  public static final ZonedDateTime DEPOSIT_START = zdt(2012, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final double DEPOSIT_NOTIONAL = -12300;
  public static final double DEPOSIT_RATE = 0.002;
  public static final ZonedDateTime IBOR_COUPON_FIXING_DATE = zdt(2011, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
  public static final double IBOR_COUPON_NOTIONAL = -45600;
  public static final double GEARING = 3.;

  public static final CashDefinition PAY_CASH = new CashDefinition(FIXED_INCOME_CURRENCY, CASH_START, CASH_MATURITY, -CASH_NOTIONAL, CASH_RATE, 0.5);
  public static final CashDefinition RECEIVE_CASH = new CashDefinition(FIXED_INCOME_CURRENCY, CASH_START, CASH_MATURITY, CASH_NOTIONAL, CASH_RATE, 0.5);
  public static final CouponFixedDefinition PAY_FIXED_COUPON = CouponFixedDefinition.from(FIXED_INCOME_CURRENCY, FIXED_COUPON_MATURITY, FIXED_COUPON_START,
      FIXED_COUPON_MATURITY, 1. / 12, -FIXED_COUPON_NOTIONAL, FIXED_COUPON_RATE);
  public static final CouponFixedDefinition RECEIVE_FIXED_COUPON = CouponFixedDefinition.from(FIXED_INCOME_CURRENCY, FIXED_COUPON_MATURITY, FIXED_COUPON_START,
      FIXED_COUPON_MATURITY, 1. / 12, FIXED_COUPON_NOTIONAL, FIXED_COUPON_RATE);
  public static final CouponIborDefinition PAY_IBOR_COUPON = CouponIborDefinition.from(-FIXED_COUPON_NOTIONAL, FIXED_COUPON_MATURITY, USD_IBOR_INDEX1, NO_HOLIDAY);
  public static final CouponIborDefinition RECEIVE_IBOR_COUPON = CouponIborDefinition.from(FIXED_COUPON_NOTIONAL, FIXED_COUPON_MATURITY, USD_IBOR_INDEX1, NO_HOLIDAY);
  public static final CouponIborGearingDefinition PAY_IBOR_GEARING_COUPON = CouponIborGearingDefinition.from(PAY_IBOR_COUPON, IBOR_COUPON_SPREAD, GEARING);
  public static final CouponIborGearingDefinition RECEIVE_IBOR_GEARING_COUPON = CouponIborGearingDefinition.from(RECEIVE_IBOR_COUPON, IBOR_COUPON_SPREAD, GEARING);
  public static final CouponIborSpreadDefinition PAY_IBOR_SPREAD_COUPON = CouponIborSpreadDefinition.from(PAY_IBOR_COUPON, IBOR_COUPON_SPREAD);
  public static final CouponIborSpreadDefinition RECEIVE_IBOR_SPREAD_COUPON = CouponIborSpreadDefinition.from(RECEIVE_IBOR_COUPON, IBOR_COUPON_SPREAD);
  public static final DepositIborDefinition PAY_IBOR_DEPOSIT = DepositIborDefinition.fromStart(DEPOSIT_START, DEPOSIT_NOTIONAL, DEPOSIT_RATE, USD_IBOR_INDEX1, NO_HOLIDAY);
  public static final DepositIborDefinition RECEIVE_IBOR_DEPOSIT = DepositIborDefinition.fromStart(DEPOSIT_START, -DEPOSIT_NOTIONAL, DEPOSIT_RATE, USD_IBOR_INDEX1, NO_HOLIDAY);
  public static final ForexDefinition FX_PAY_GBP = ForexDefinition.fromAmounts(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_MATURITY, FX_PAY_AMOUNT, FX_RECEIVE_AMOUNT);
  public static final ForexDefinition FX_PAY_EUR = ForexDefinition.fromAmounts(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_MATURITY, -FX_PAY_AMOUNT, -FX_RECEIVE_AMOUNT);
  public static final ForexNonDeliverableForwardDefinition LONG_NDF = new ForexNonDeliverableForwardDefinition(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, -FX_PAY_AMOUNT,
      -FX_RECEIVE_AMOUNT / FX_PAY_AMOUNT, FX_MATURITY, FX_MATURITY);
  public static final ForexNonDeliverableForwardDefinition SHORT_NDF = new ForexNonDeliverableForwardDefinition(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_PAY_AMOUNT,
      -FX_RECEIVE_AMOUNT / FX_PAY_AMOUNT, FX_MATURITY, FX_MATURITY);
  public static final ForwardRateAgreementDefinition PAYER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, FRA_NOTIONAL, USD_IBOR_INDEX1, FRA_RATE, NO_HOLIDAY);
  public static final ForwardRateAgreementDefinition RECEIVER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, -FRA_NOTIONAL, USD_IBOR_INDEX1, FRA_RATE, NO_HOLIDAY);
  public static final PaymentFixedDefinition PAY_FIXED_PAYMENT = new PaymentFixedDefinition(FIXED_INCOME_CURRENCY, PAYMENT_MATURITY, -PAYMENT_AMOUNT);
  public static final PaymentFixedDefinition RECEIVE_FIXED_PAYMENT = new PaymentFixedDefinition(FIXED_INCOME_CURRENCY, PAYMENT_MATURITY, PAYMENT_AMOUNT);
  public static final SwapFixedIborDefinition PAYER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE, true);
  public static final SwapFixedIborDefinition RECEIVER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE,
      false);
  public static final SwapFixedIborSpreadDefinition PAYER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL,
      SWAP_NOTIONAL, SWAP_FIXED_RATE, IBOR_SPREAD, true);
  public static final SwapFixedIborSpreadDefinition RECEIVER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR,
      SWAP_NOTIONAL, SWAP_NOTIONAL, SWAP_FIXED_RATE, IBOR_SPREAD, false);
  public static final SwapIborIborDefinition PAY_SPREAD_IBOR_IBOR_SWAP = SwapIborIborDefinition.from(SWAP_START, Period.ofYears(50), IBOR_IBOR_GENERATOR,
      SWAP_NOTIONAL, IBOR_SPREAD, true);
  public static final SwapIborIborDefinition RECEIVE_SPREAD_IBOR_IBOR_SWAP = SwapIborIborDefinition.from(SWAP_START, Period.ofYears(50), IBOR_IBOR_GENERATOR,
      SWAP_NOTIONAL, IBOR_SPREAD, false);
  public static final DoubleTimeSeries<LocalDate> IBOR_FIXING_SERIES;
  public static final double FIXING_RATE = 0.03;
  public static final LocalDate TODAY = LocalDate.of(2012, 8, 1);

  static {
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> fixings = new ArrayList<>();
    LocalDate date = LocalDate.of(2000, 1, 1);
    while (date.isBefore(TODAY)) {
      dates.add(date);
      fixings.add(FIXING_RATE);
      date = date.plusDays(1);
    }
    IBOR_FIXING_SERIES = ImmutableLocalDateDoubleTimeSeries.of(dates, fixings);
  }

  public static final class SemiAnnualDayCount extends AbstractDayCount {

    public SemiAnnualDayCount() {
    }

    @Override
    public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
      return 0.5;
    }

    @Override
    public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon,
        final double paymentsPerYear) {
      return 0;
    }

    @Override
    public String getName() {
      return null;
    }

  }

  public static final class QuarterlyDayCount extends AbstractDayCount {

    public QuarterlyDayCount() {
    }

    @Override
    public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
      return 0.25;
    }

    @Override
    public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon,
        final double paymentsPerYear) {
      return 0;
    }

    @Override
    public String getName() {
      return null;
    }

  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
