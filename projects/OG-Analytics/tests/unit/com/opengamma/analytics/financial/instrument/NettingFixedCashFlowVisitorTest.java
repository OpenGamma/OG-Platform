/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_EUR;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_GBP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_FIXING_SERIES;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.LONG_NDF;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_SPREAD_IBOR_IBOR_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_SPREAD_IBOR_IBOR_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SHORT_NDF;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class NettingFixedCashFlowVisitorTest {
  private static final NettingFixedCashFlowVisitor VISITOR = NettingFixedCashFlowVisitor.getInstance();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_PAY_INSTRUMENTS = new HashSet<InstrumentDefinition<?>>();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_RECEIVE_INSTRUMENTS = new HashSet<InstrumentDefinition<?>>();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_MULTIPLE_CASHFLOWS = new HashSet<InstrumentDefinition<?>>();
  private static final Set<InstrumentDefinition<?>> NETTING_INSTRUMENTS = new HashSet<InstrumentDefinition<?>>();
  private static final FixedPayCashFlowVisitor PAY_CASH_FLOWS = FixedPayCashFlowVisitor.getInstance();
  private static final FixedReceiveCashFlowVisitor RECEIVE_CASH_FLOWS = FixedReceiveCashFlowVisitor.getInstance();

  static {
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_CASH);
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_FIXED_COUPON);
    NO_NETTING_PAY_INSTRUMENTS.add(SHORT_NDF);
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_FIXED_PAYMENT);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_CASH);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_FIXED_COUPON);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(LONG_NDF);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_FIXED_PAYMENT);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(FX_PAY_EUR);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(FX_PAY_GBP);
    NETTING_INSTRUMENTS.add(PAYER_FRA);
    NETTING_INSTRUMENTS.add(RECEIVER_SWAP);
    NETTING_INSTRUMENTS.add(PAYER_SWAP);
    NETTING_INSTRUMENTS.add(RECEIVER_SWAP);
    NETTING_INSTRUMENTS.add(PAYER_SWAP_WITH_SPREAD);
    NETTING_INSTRUMENTS.add(RECEIVER_SWAP_WITH_SPREAD);
    NETTING_INSTRUMENTS.add(PAY_SPREAD_IBOR_IBOR_SWAP);
    NETTING_INSTRUMENTS.add(RECEIVE_SPREAD_IBOR_IBOR_SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument1() {
    VISITOR.visit(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument2() {
    VISITOR.visit(null, IBOR_FIXING_SERIES);
  }

  @Test
  public void testPayCashFlowsNoNetting() {
    for (final InstrumentDefinition<?> definition : NO_NETTING_PAY_INSTRUMENTS) {
      final TreeMap<LocalDate, MultipleCurrencyAmount> pay = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(PAY_CASH_FLOWS));
      final TreeMap<LocalDate, MultipleCurrencyAmount> netted = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(VISITOR));
      assertEquals(pay.size(), netted.size());
      assertEquals(pay.keySet(), netted.keySet());
      final Iterator<Map.Entry<LocalDate, MultipleCurrencyAmount>> nettedIterator = netted.entrySet().iterator();
      for (final Map.Entry<LocalDate, MultipleCurrencyAmount> payEntry : pay.entrySet()) {
        final Map.Entry<LocalDate, MultipleCurrencyAmount> nettedEntry = nettedIterator.next();
        assertEquals(payEntry.getKey(), nettedEntry.getKey());
        assertEquals(payEntry.getValue().size(), nettedEntry.getValue().size());
        final Iterator<CurrencyAmount> nettedMCAIterator = nettedEntry.getValue().iterator();
        for (final CurrencyAmount payCA : payEntry.getValue()) {
          final CurrencyAmount nettedCA = nettedMCAIterator.next();
          assertEquals(payCA.getCurrency(), nettedCA.getCurrency());
          assertEquals(payCA.getAmount(), -nettedCA.getAmount());
        }
      }
    }
  }

  @Test
  public void testReceiveCashFlowsNoNetting() {
    for (final InstrumentDefinition<?> definition : NO_NETTING_RECEIVE_INSTRUMENTS) {
      final TreeMap<LocalDate, MultipleCurrencyAmount> pay = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(RECEIVE_CASH_FLOWS));
      final TreeMap<LocalDate, MultipleCurrencyAmount> netted = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(VISITOR));
      assertEquals(pay.size(), netted.size());
      assertEquals(pay.keySet(), netted.keySet());
      final Iterator<Map.Entry<LocalDate, MultipleCurrencyAmount>> nettedIterator = netted.entrySet().iterator();
      for (final Map.Entry<LocalDate, MultipleCurrencyAmount> payEntry : pay.entrySet()) {
        final Map.Entry<LocalDate, MultipleCurrencyAmount> nettedEntry = nettedIterator.next();
        assertEquals(payEntry.getKey(), nettedEntry.getKey());
        assertEquals(payEntry.getValue().size(), nettedEntry.getValue().size());
        final Iterator<CurrencyAmount> nettedMCAIterator = nettedEntry.getValue().iterator();
        for (final CurrencyAmount payCA : payEntry.getValue()) {
          final CurrencyAmount nettedCA = nettedMCAIterator.next();
          assertEquals(payCA.getCurrency(), nettedCA.getCurrency());
          assertEquals(payCA.getAmount(), nettedCA.getAmount());
        }
      }
    }
  }
}
