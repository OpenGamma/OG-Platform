/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;

/**
 * 
 */
public class FixedReceiveCashFlowVisitorTest {
  private static final Calendar NO_HOLIDAY = new NoHolidayCalendar();
  private static final DayCount DAY_COUNT = new SemiAnnualDayCount();
  private static final BusinessDayConvention NONE = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("None");
  private static final Currency FIXED_INCOME_CURRENCY = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(FIXED_INCOME_CURRENCY, Period.ofMonths(6), 0, NO_HOLIDAY, DAY_COUNT, NONE, false, "f");
  private static final ZonedDateTime CASH_START = ZonedDateTime.of(2012, 6, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final ZonedDateTime CASH_MATURITY = ZonedDateTime.of(2012, 12, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final double CASH_NOTIONAL = 234000;
  private static final double CASH_RATE = 0.002;
  private static final CashDefinition CASH = new CashDefinition(FIXED_INCOME_CURRENCY, CASH_START, CASH_MATURITY, CASH_NOTIONAL, CASH_RATE, 0.5);
  private static final ZonedDateTime PAYMENT_MATURITY = ZonedDateTime.of(2011, 1, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final double PAYMENT_AMOUNT = 34500;
  private static final PaymentFixedDefinition FIXED_PAYMENT = new PaymentFixedDefinition(FIXED_INCOME_CURRENCY, PAYMENT_MATURITY, PAYMENT_AMOUNT);
  private static final ZonedDateTime FIXED_COUPON_START = ZonedDateTime.of(2011, 1, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final ZonedDateTime FIXED_COUPON_MATURITY = ZonedDateTime.of(2011, 2, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final double FIXED_COUPON_NOTIONAL = 45600;
  private static final double FIXED_COUPON_RATE = 0.0001;
  private static final CouponFixedDefinition FIXED_COUPON = CouponFixedDefinition.from(FIXED_INCOME_CURRENCY, FIXED_COUPON_MATURITY, FIXED_COUPON_START,
      FIXED_COUPON_MATURITY, 1. / 12, FIXED_COUPON_NOTIONAL, FIXED_COUPON_RATE);
  private static final CouponIborDefinition IBOR_COUPON = CouponIborDefinition.from(FIXED_COUPON_NOTIONAL, FIXED_COUPON_MATURITY, IBOR_INDEX);
  private static final double IBOR_COUPON_SPREAD = 0.00023;
  private static final CouponIborSpreadDefinition IBOR_SPREAD_COUPON = CouponIborSpreadDefinition.from(IBOR_COUPON, IBOR_COUPON_SPREAD);
  private static final ZonedDateTime FRA_START = ZonedDateTime.of(2011, 6, 3, 11, 0, 0, 0, TimeZone.UTC);
  private static final ZonedDateTime FRA_END = ZonedDateTime.of(2011, 12, 3, 11, 0, 0, 0, TimeZone.UTC);
  private static final double FRA_NOTIONAL = 567000;
  private static final double FRA_RATE = 0.004;
  private static final ForwardRateAgreementDefinition PAYER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, FRA_NOTIONAL, IBOR_INDEX, FRA_RATE);
  private static final ForwardRateAgreementDefinition RECEIVER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, -FRA_NOTIONAL, IBOR_INDEX, FRA_RATE);
  private static final ZonedDateTime SWAP_START = ZonedDateTime.of(2001, 1, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final ZonedDateTime SWAP_MATURITY = ZonedDateTime.of(2031, 1, 1, 11, 0, 0, 0, TimeZone.UTC);
  private static final GeneratorSwapFixedIbor SWAP_GENERATOR = new GeneratorSwapFixedIbor("a", Period.ofMonths(6), DAY_COUNT, IBOR_INDEX);
  private static final double SWAP_NOTIONAL = 789000;
  private static final double SWAP_FIXED_RATE = 0.04;
  private static final SwapFixedIborDefinition PAYER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE, true);
  private static final SwapFixedIborDefinition RECEIVER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE,
      false);
  private static final double IBOR_SPREAD = 0.01;
  private static final SwapFixedIborSpreadDefinition PAYER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR,
      SWAP_NOTIONAL, SWAP_NOTIONAL, SWAP_FIXED_RATE, IBOR_SPREAD, true);
  private static final SwapFixedIborSpreadDefinition RECEIVER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR,
      SWAP_NOTIONAL, SWAP_NOTIONAL, SWAP_FIXED_RATE, IBOR_SPREAD, false);
  private static final Currency FX_PAY_CURRENCY = Currency.GBP;
  private static final Currency FX_RECEIVE_CURRENCY = Currency.EUR;
  private static final ZonedDateTime FX_MATURITY = DateUtils.getUTCDate(2013, 1, 1);
  private static final double FX_PAY_AMOUNT = -12345;
  private static final double FX_RECEIVE_AMOUNT = 23456;
  private static final ForexDefinition FX_PAY_GBP = ForexDefinition.fromAmounts(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_MATURITY, FX_PAY_AMOUNT, FX_RECEIVE_AMOUNT);
  private static final ForexDefinition FX_PAY_EUR = ForexDefinition.fromAmounts(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_MATURITY, -FX_PAY_AMOUNT, -FX_RECEIVE_AMOUNT);
  private static final ForexNonDeliverableForwardDefinition LONG_NDF = new ForexNonDeliverableForwardDefinition(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, -FX_PAY_AMOUNT,
      -FX_RECEIVE_AMOUNT / FX_PAY_AMOUNT, FX_MATURITY, FX_MATURITY);
  private static final ForexNonDeliverableForwardDefinition SHORT_NDF = new ForexNonDeliverableForwardDefinition(FX_PAY_CURRENCY, FX_RECEIVE_CURRENCY, FX_PAY_AMOUNT,
      -FX_RECEIVE_AMOUNT / FX_PAY_AMOUNT, FX_MATURITY, FX_MATURITY);
  private static final DoubleTimeSeries<LocalDate> IBOR_FIXING_SERIES;
  private static final double FIXING_RATE = 0.03;
  private static final LocalDate TODAY = LocalDate.of(2012, 8, 1);
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITHOUT_FIXINGS;
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES;
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES;
  private static final FixedReceiveCashFlowVisitor VISITOR = FixedReceiveCashFlowVisitor.getInstance();

  static {
    INSTRUMENTS_WITHOUT_FIXINGS = new HashSet<InstrumentDefinition<?>>();
    INSTRUMENTS_WITHOUT_FIXINGS.add(CASH);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FIXED_PAYMENT);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FIXED_COUPON);
    INSTRUMENTS_WITHOUT_FIXINGS.add(RECEIVER_FRA);
    INSTRUMENTS_WITHOUT_FIXINGS.add(RECEIVER_SWAP);
    INSTRUMENTS_WITHOUT_FIXINGS.add(RECEIVER_SWAP_WITH_SPREAD);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FX_PAY_GBP);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FX_PAY_EUR);
    INSTRUMENTS_WITHOUT_FIXINGS.add(LONG_NDF);
    INSTRUMENTS_WITHOUT_FIXINGS.add(SHORT_NDF);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES = new HashSet<InstrumentDefinition<?>>();
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(PAYER_FRA);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(PAYER_SWAP);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(PAYER_SWAP_WITH_SPREAD);
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES = new HashSet<InstrumentDefinition<?>>();
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES.add(IBOR_COUPON);
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES.add(IBOR_SPREAD_COUPON);
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    final List<Double> fixings = new ArrayList<Double>();
    LocalDate date = LocalDate.of(2000, 1, 1);
    while (date.isBefore(TODAY)) {
      dates.add(date);
      fixings.add(FIXING_RATE);
      date = date.plusDays(1);
    }
    IBOR_FIXING_SERIES = new ListLocalDateDoubleTimeSeries(dates, fixings);
  }

  @Test
  public void testNoFixingData() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES) {
      try {
        definition.accept(VISITOR);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES) {
      try {
        definition.accept(VISITOR);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  @Test
  public void testNoFixingNeeded() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITHOUT_FIXINGS) {
      assertEquals(definition.accept(VISITOR), definition.accept(VISITOR, null));
    }
  }

  @Test
  public void testMissingFixingData() {
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    final List<Double> fixings = new ArrayList<Double>();
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate endDate = LocalDate.of(2014, 1, 1);
    while (date.isBefore(endDate)) {
      dates.add(date);
      fixings.add(FIXING_RATE);
      date = date.plusDays(1);
    }
    final ListLocalDateDoubleTimeSeries fixingSeries = new ListLocalDateDoubleTimeSeries(dates, fixings);
    final Set<InstrumentDefinition<?>> floatingInstruments = new HashSet<InstrumentDefinition<?>>(INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES);
    floatingInstruments.addAll(INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES);
    for (final InstrumentDefinition<?> definition : floatingInstruments) {
      try {
        definition.accept(VISITOR, fixingSeries);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }

  @Test
  public void testCash() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = CASH.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(CASH_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(CASH_NOTIONAL * CASH_RATE * 0.5, ca.getAmount());
  }

  @Test
  public void testFixedPayment() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = FIXED_PAYMENT.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAYMENT_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(PAYMENT_AMOUNT, ca.getAmount());
  }

  @Test
  public void testFixedCoupon() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = FIXED_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * FIXED_COUPON_RATE / 12, ca.getAmount(), 1e-15);
  }

  @Test
  public void testIborCoupon() {
    Map<LocalDate, MultipleCurrencyAmount> payment = IBOR_COUPON.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate().plusMonths(6), Iterables.getOnlyElement(payment.keySet()));
    MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * FIXING_RATE / 2, ca.getAmount(), 1e-15);
    payment = IBOR_SPREAD_COUPON.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate().plusMonths(6), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * (FIXING_RATE + IBOR_COUPON_SPREAD) / 2, ca.getAmount(), 1e-15);
  }

  @Test
  public void testPayerFRA() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = PAYER_FRA.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FRA_START.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * FIXING_RATE * 0.5, ca.getAmount(), 1e-15);
  }

  @Test
  public void testReceiverFRA() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = RECEIVER_FRA.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FRA_START.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * FRA_RATE * 0.5, ca.getAmount(), 1e-15);
  }

  @Test
  public void testPayerSwap() {
    Map<LocalDate, MultipleCurrencyAmount> payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(PAYER_SWAP.accept(VISITOR, IBOR_FIXING_SERIES));
    assertEquals(24, payments.size());
    LocalDate paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * FIXING_RATE / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(PAYER_SWAP_WITH_SPREAD.accept(VISITOR, IBOR_FIXING_SERIES));
    assertEquals(24, payments.size());
    paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * (FIXING_RATE + IBOR_SPREAD) / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
  }

  @Test
  public void testReceiverSwap() {
    Map<LocalDate, MultipleCurrencyAmount> payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(RECEIVER_SWAP.accept(VISITOR));
    assertEquals(60, payments.size());
    LocalDate paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR));
    assertEquals(60, payments.size());
    paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
  }

  @Test
  public void testFX() {
    Map<LocalDate, MultipleCurrencyAmount> payment = FX_PAY_GBP.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    CurrencyAmount amount = Iterables.getOnlyElement(payment.values()).getCurrencyAmounts()[0];
    assertEquals(CurrencyAmount.of(FX_RECEIVE_CURRENCY, FX_RECEIVE_AMOUNT), amount);
    payment = FX_PAY_EUR.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    amount = Iterables.getOnlyElement(payment.values()).getCurrencyAmounts()[0];
    assertEquals(CurrencyAmount.of(FX_PAY_CURRENCY, -FX_PAY_AMOUNT), amount);
  }

  @Test
  public void testNDF() {
    Map<LocalDate, MultipleCurrencyAmount> payment = SHORT_NDF.accept(VISITOR);
    assertEquals(0, payment.size());
    payment = LONG_NDF.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final CurrencyAmount amount = Iterables.getOnlyElement(payment.values()).getCurrencyAmounts()[0];
    assertEquals(CurrencyAmount.of(FX_RECEIVE_CURRENCY, -FX_PAY_AMOUNT), amount);
  }

  private static final class SemiAnnualDayCount extends DayCount {

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
    public String getConventionName() {
      return null;
    }

  }
}
