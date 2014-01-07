/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.DEPOSIT_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.DEPOSIT_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXED_INCOME_CURRENCY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FRA_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.GEARING;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_COUPON_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_COUPON_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_DEPOSIT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_GEARING_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_SPREAD_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_SPREAD_IBOR_IBOR_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_DEPOSIT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_GEARING_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_SPREAD_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_SPREAD_IBOR_IBOR_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SWAP_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SWAP_START;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FloatingPayCashFlowVisitorTest {
  private static final FloatingPayCashFlowVisitor VISITOR = FloatingPayCashFlowVisitor.getInstance();

  @Test
  public void testIborDeposit() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = PAY_IBOR_DEPOSIT.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAY_IBOR_DEPOSIT.getEndDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-DEPOSIT_NOTIONAL * DEPOSIT_RATE / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, PAY_IBOR_DEPOSIT.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_DEPOSIT.accept(VISITOR));
  }

  @Test
  public void testIborCoupon() {
    Map<LocalDate, MultipleCurrencyAmount> payment = PAY_IBOR_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAY_IBOR_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-IBOR_COUPON_NOTIONAL / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, PAY_IBOR_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_COUPON.accept(VISITOR));
    payment = PAY_IBOR_SPREAD_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAY_IBOR_SPREAD_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-IBOR_COUPON_NOTIONAL / 2. - IBOR_COUPON_NOTIONAL * IBOR_COUPON_SPREAD / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, PAY_IBOR_SPREAD_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_SPREAD_COUPON.accept(VISITOR));
    payment = PAY_IBOR_GEARING_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAY_IBOR_GEARING_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-IBOR_COUPON_NOTIONAL * GEARING / 2. - IBOR_COUPON_NOTIONAL * IBOR_COUPON_SPREAD / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, PAY_IBOR_GEARING_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_GEARING_COUPON.accept(VISITOR));
  }

  @Test
  public void testFRA() {
    final Map<LocalDate, MultipleCurrencyAmount> payment = RECEIVER_FRA.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(RECEIVER_FRA.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultipleCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca);
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * 0.5, ca.getAmount());
    assertEquals(payment, RECEIVER_FRA.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), PAYER_FRA.accept(VISITOR));
  }

  @Test
  public void testSwap() {
    Map<LocalDate, MultipleCurrencyAmount> payments = new TreeMap<>(RECEIVER_SWAP.accept(VISITOR));
    assertEquals(60, payments.size());
    LocalDate paymentDate = SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultipleCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca);
      assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(SWAP_NOTIONAL * 0.5, ca.getAmount());
    }
    assertEquals(payments, RECEIVER_SWAP.accept(VISITOR, null));
    payments = new TreeMap<>(RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR));
    assertEquals(60, payments.size());
    paymentDate = SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultipleCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca);
      assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(SWAP_NOTIONAL / 2. + SWAP_NOTIONAL * IBOR_SPREAD / 2., ca.getAmount());
    }
    assertEquals(payments, RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), PAYER_SWAP.accept(VISITOR));
    assertEquals(Collections.emptyMap(), PAYER_SWAP_WITH_SPREAD.accept(VISITOR));
  }

  @Test
  public void testIborIborSwap() {
    Map<LocalDate, MultipleCurrencyAmount> payments = new TreeMap<>(PAY_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR));
    assertEquals(100, payments.size());
    LocalDate paymentDate = SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultipleCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca);
      assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(SWAP_NOTIONAL / 2. + SWAP_NOTIONAL * IBOR_SPREAD / 2., ca.getAmount());
    }
    assertEquals(payments, PAY_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR, null));
    payments = new TreeMap<>(RECEIVE_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR));
    assertEquals(200, payments.size());
    paymentDate = SWAP_START.plusMonths(3).toLocalDate();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(3);
      final MultipleCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca);
      assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(SWAP_NOTIONAL / 4., ca.getAmount());
    }
    assertEquals(payments, RECEIVE_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR, null));
  }
}
