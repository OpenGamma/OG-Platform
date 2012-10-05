/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class FixedReceiveCashFlowVisitorTest {
  private static final Calendar NO_HOLIDAY = new NoHolidayCalendar();
  private static final DayCount DAY_COUNT = new SemiAnnualDayCount();
  private static final BusinessDayConvention NONE = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("None");
  private static final Currency FIXED_INCOME_CURRENCY = Currency.USD;
  private static final YieldConvention BOND_YIELD = SimpleYieldConvention.US_STREET;
  private static final ZonedDateTime BOND_MATURITY = DateUtils.getUTCDate(2020, 1, 15);
  private static final ZonedDateTime BOND_FIRST_ACCRUAL = DateUtils.getUTCDate(2000, 1, 15);
  private static final Period BOND_FREQUENCY = Period.ofMonths(6);
  private static final double BOND_RATE = 0.03;
  private static final BondFixedSecurityDefinition BOND = BondFixedSecurityDefinition.from(FIXED_INCOME_CURRENCY, BOND_MATURITY, BOND_FIRST_ACCRUAL, BOND_FREQUENCY,
      BOND_RATE, 0, NO_HOLIDAY, DAY_COUNT, NONE, BOND_YIELD, false);
  private static final BondFixedTransactionDefinition BOND_TRADE = new BondFixedTransactionDefinition(BOND, 1000, BOND_FIRST_ACCRUAL, 1);
  private static final ZonedDateTime BILL_MATURITY = DateUtils.getUTCDate(2012, 12, 9);
  private static final double BILL_NOTIONAL = 12300;
  private static final BillSecurityDefinition BILL = new BillSecurityDefinition(FIXED_INCOME_CURRENCY, BILL_MATURITY, BILL_NOTIONAL, 0, NO_HOLIDAY, BOND_YIELD,
      DAY_COUNT, "d");
  private static final BillTransactionDefinition BILL_TRADE = new BillTransactionDefinition(BILL, 1000, BILL_MATURITY, -1000);
  private static final ZonedDateTime CASH_START = DateUtils.getUTCDate(2012, 6, 1);
  private static final ZonedDateTime CASH_MATURITY = DateUtils.getUTCDate(2012, 12, 1);
  private static final double CASH_NOTIONAL = 234000;
  private static final double CASH_RATE = 0.002;
  private static final CashDefinition CASH = new CashDefinition(FIXED_INCOME_CURRENCY, CASH_START, CASH_MATURITY, CASH_NOTIONAL, CASH_RATE, 0.5);
  private static final ZonedDateTime PAYMENT_MATURITY = DateUtils.getUTCDate(2013, 1, 1);
  private static final double PAYMENT_AMOUNT = 34500;
  private static final PaymentFixedDefinition FIXED_PAYMENT = new PaymentFixedDefinition(FIXED_INCOME_CURRENCY, PAYMENT_MATURITY, PAYMENT_AMOUNT);
  private static final ZonedDateTime FIXED_COUPON_START = DateUtils.getUTCDate(2013, 1, 1);
  private static final ZonedDateTime FIXED_COUPON_MATURITY = DateUtils.getUTCDate(2013, 2, 1);
  private static final double FIXED_COUPON_NOTIONAL = 45600;
  private static final double FIXED_COUPON_RATE = 0.0001;
  private static final CouponFixedDefinition FIXED_COUPON = CouponFixedDefinition.from(FIXED_INCOME_CURRENCY, FIXED_COUPON_MATURITY, FIXED_COUPON_START,
      FIXED_COUPON_MATURITY, 1. / 12, FIXED_COUPON_NOTIONAL, FIXED_COUPON_RATE);
  private static final ZonedDateTime FRA_START = DateUtils.getUTCDate(2013, 6, 3);
  private static final ZonedDateTime FRA_END = DateUtils.getUTCDate(2013, 12, 3);
  private static final double FRA_NOTIONAL = 567000;
  private static final IborIndex IBOR_INDEX = new IborIndex(FIXED_INCOME_CURRENCY, Period.ofMonths(6), 0, NO_HOLIDAY, DAY_COUNT, NONE, false, "f");
  private static final double FRA_RATE = 0.004;
  private static final ForwardRateAgreementDefinition PAYER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, FRA_NOTIONAL, IBOR_INDEX, FRA_RATE);
  private static final ForwardRateAgreementDefinition RECEIVER_FRA = ForwardRateAgreementDefinition.from(FRA_START, FRA_END, -FRA_NOTIONAL, IBOR_INDEX, FRA_RATE);
  private static final ZonedDateTime SWAP_START = DateUtils.getUTCDate(2001, 1, 1);
  private static final ZonedDateTime SWAP_MATURITY = DateUtils.getUTCDate(2031, 1, 1);
  private static final GeneratorSwapFixedIbor SWAP_GENERATOR = new GeneratorSwapFixedIbor("a", Period.ofMonths(6), DAY_COUNT, IBOR_INDEX);
  private static final double SWAP_NOTIONAL = 789000;
  private static final double SWAP_FIXED_RATE = 0.04;
  private static final SwapFixedIborDefinition PAYER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE, true);
  private static final SwapFixedIborDefinition RECEIVER_SWAP = SwapFixedIborDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR, SWAP_NOTIONAL, SWAP_FIXED_RATE,
      false);
  private static final SwapFixedIborSpreadDefinition PAYER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR,
      SWAP_NOTIONAL, SWAP_NOTIONAL, SWAP_FIXED_RATE, 0.01, true);
  private static final SwapFixedIborSpreadDefinition RECEIVER_SWAP_WITH_SPREAD = SwapFixedIborSpreadDefinition.from(SWAP_START, SWAP_MATURITY, SWAP_GENERATOR,
      SWAP_NOTIONAL, SWAP_NOTIONAL, SWAP_FIXED_RATE, 0.01, false);
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
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS;
  private static final FixedReceiveCashFlowVisitor VISITOR = FixedReceiveCashFlowVisitor.getInstance();

  static {
    INSTRUMENTS = new HashSet<InstrumentDefinition<?>>();
    INSTRUMENTS.add(BOND);
    INSTRUMENTS.add(BOND_TRADE);
    INSTRUMENTS.add(BILL);
    INSTRUMENTS.add(BILL_TRADE);
    INSTRUMENTS.add(CASH);
    INSTRUMENTS.add(FIXED_PAYMENT);
    INSTRUMENTS.add(FIXED_COUPON);
    INSTRUMENTS.add(PAYER_FRA);
    INSTRUMENTS.add(RECEIVER_FRA);
    INSTRUMENTS.add(PAYER_SWAP);
    INSTRUMENTS.add(RECEIVER_SWAP);
    INSTRUMENTS.add(PAYER_SWAP_WITH_SPREAD);
    INSTRUMENTS.add(RECEIVER_SWAP_WITH_SPREAD);
    INSTRUMENTS.add(FX_PAY_GBP);
    INSTRUMENTS.add(FX_PAY_EUR);
    INSTRUMENTS.add(LONG_NDF);
    INSTRUMENTS.add(SHORT_NDF);
  }

  @Test
  public void testNoDate() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS) {
      try {
        definition.accept(VISITOR);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  @Test
  public void testNullDate() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS) {
      try {
        definition.accept(VISITOR, null);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }

  @Test
  public void testInstrumentsAfterExpiry() {
    final LocalDate date = LocalDate.of(2100, 1, 1);
    for (final InstrumentDefinition<?> definition : INSTRUMENTS) {
      assertEquals(0, definition.accept(VISITOR, date).size());
    }
  }

  @Test
  public void testBond() {
    final LocalDate date = LocalDate.of(2012, 9, 1);
    final Map<LocalDate, MultipleCurrencyAmount> remainingPayments = new TreeMap<LocalDate, MultipleCurrencyAmount>(BOND.accept(VISITOR, date));
    assertEquals(remainingPayments, new TreeMap<LocalDate, MultipleCurrencyAmount>(BOND_TRADE.accept(VISITOR, date)));
    assertEquals(15, remainingPayments.size());
    final LocalDate firstPayment = LocalDate.of(2013, 1, 15);
    int i = 0;
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : remainingPayments.entrySet()) {
      assertEquals(firstPayment.plusMonths(6 * i++), entry.getKey());
      if (i == remainingPayments.size()) {
        assertEquals(MultipleCurrencyAmount.of(CurrencyAmount.of(FIXED_INCOME_CURRENCY, 1 + BOND_RATE / 2.)), entry.getValue());
      } else {
        assertEquals(MultipleCurrencyAmount.of(CurrencyAmount.of(FIXED_INCOME_CURRENCY, BOND_RATE / 2.)), entry.getValue());
      }
    }
  }

  @Test
  public void testBill() {
    final LocalDate date = LocalDate.of(2012, 10, 1);
    final Map<LocalDate, MultipleCurrencyAmount> payment = BILL.accept(VISITOR, date);
    assertEquals(1, payment.size());
    assertEquals(BILL_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(BILL_NOTIONAL, ca.getAmount());
    assertEquals(payment, BILL_TRADE.accept(VISITOR, date));
  }

  @Test
  public void testCash() {
    final LocalDate date = LocalDate.of(2012, 8, 1);
    final Map<LocalDate, MultipleCurrencyAmount> payment = CASH.accept(VISITOR, date);
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
    final LocalDate date = LocalDate.of(2012, 8, 1);
    final Map<LocalDate, MultipleCurrencyAmount> payment = FIXED_PAYMENT.accept(VISITOR, date);
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
    final LocalDate date = LocalDate.of(2012, 8, 1);
    final Map<LocalDate, MultipleCurrencyAmount> payment = FIXED_COUPON.accept(VISITOR, date);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * FIXED_COUPON_RATE / 12, ca.getAmount(), 1e-15);
  }

  @Test
  public void testFRA() {
    final LocalDate date = LocalDate.of(2012, 8, 1);
    Map<LocalDate, MultipleCurrencyAmount> payment = RECEIVER_FRA.accept(VISITOR, date);
    assertEquals(1, payment.size());
    assertEquals(FRA_START.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * FRA_RATE * 0.5, ca.getAmount(), 1e-15);
    payment = PAYER_FRA.accept(VISITOR, date);
    assertTrue(payment.isEmpty());
  }

  @Test
  public void testSwap() {
    final LocalDate date = LocalDate.of(2012, 5, 1);
    Map<LocalDate, MultipleCurrencyAmount> payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(RECEIVER_SWAP.accept(VISITOR, date));
    assertEquals(38, payments.size());
    LocalDate paymentDate = LocalDate.of(2012, 7, 1);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = PAYER_SWAP.accept(VISITOR, date);
    assertTrue(payments.isEmpty());
    payments = new TreeMap<LocalDate, MultipleCurrencyAmount>(RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR, date));
    assertEquals(38, payments.size());
    paymentDate = LocalDate.of(2012, 7, 1);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().getCurrencyAmounts()[0].getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().getCurrencyAmounts()[0].getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = PAYER_SWAP_WITH_SPREAD.accept(VISITOR, date);
    assertTrue(payments.isEmpty());
  }

  @Test
  public void testFX() {
    final LocalDate date = LocalDate.of(2012, 10, 1);
    Map<LocalDate, MultipleCurrencyAmount> payment = FX_PAY_GBP.accept(VISITOR, date);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    CurrencyAmount amount = Iterables.getOnlyElement(payment.values()).getCurrencyAmounts()[0];
    assertEquals(CurrencyAmount.of(FX_RECEIVE_CURRENCY, FX_RECEIVE_AMOUNT), amount);
    payment = FX_PAY_EUR.accept(VISITOR, date);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    amount = Iterables.getOnlyElement(payment.values()).getCurrencyAmounts()[0];
    assertEquals(CurrencyAmount.of(FX_PAY_CURRENCY, -FX_PAY_AMOUNT), amount);
  }

  @Test
  public void testNDF() {
    final LocalDate date = LocalDate.of(2012, 10, 1);
    Map<LocalDate, MultipleCurrencyAmount> payment = SHORT_NDF.accept(VISITOR, date);
    assertEquals(0, payment.size());
    payment = LONG_NDF.accept(VISITOR, date);
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
