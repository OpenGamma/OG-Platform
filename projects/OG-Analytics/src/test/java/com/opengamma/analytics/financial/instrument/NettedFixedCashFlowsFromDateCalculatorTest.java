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
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SHORT_NDF;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.TODAY;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NettedFixedCashFlowsFromDateCalculatorTest {
  private static final NettedFixedCashFlowFromDateCalculator CALCULATOR = NettedFixedCashFlowFromDateCalculator.getInstance();
  private static final InstrumentDefinitionVisitor<DoubleTimeSeries<LocalDate>, Map<LocalDate, MultipleCurrencyAmount>> ALL_CASH_FLOWS = NettedFixedCashFlowVisitor.getVisitor();
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS = new HashSet<>();
  private static final Set<InstrumentDefinition<?>> NON_FIXING_INSTRUMENTS = new HashSet<>();

  static {
    NON_FIXING_INSTRUMENTS.add(PAY_CASH);
    NON_FIXING_INSTRUMENTS.add(PAY_FIXED_COUPON);
    NON_FIXING_INSTRUMENTS.add(SHORT_NDF);
    NON_FIXING_INSTRUMENTS.add(PAY_FIXED_PAYMENT);
    NON_FIXING_INSTRUMENTS.add(RECEIVE_CASH);
    NON_FIXING_INSTRUMENTS.add(RECEIVE_FIXED_COUPON);
    NON_FIXING_INSTRUMENTS.add(LONG_NDF);
    NON_FIXING_INSTRUMENTS.add(RECEIVE_FIXED_PAYMENT);
    NON_FIXING_INSTRUMENTS.add(FX_PAY_EUR);
    NON_FIXING_INSTRUMENTS.add(FX_PAY_GBP);
    INSTRUMENTS.add(PAY_CASH);
    INSTRUMENTS.add(PAY_FIXED_COUPON);
    INSTRUMENTS.add(SHORT_NDF);
    INSTRUMENTS.add(PAY_FIXED_PAYMENT);
    INSTRUMENTS.add(RECEIVE_CASH);
    INSTRUMENTS.add(RECEIVE_FIXED_COUPON);
    INSTRUMENTS.add(LONG_NDF);
    INSTRUMENTS.add(RECEIVE_FIXED_PAYMENT);
    INSTRUMENTS.add(FX_PAY_EUR);
    INSTRUMENTS.add(FX_PAY_GBP);
    INSTRUMENTS.add(PAYER_FRA);
    INSTRUMENTS.add(RECEIVER_SWAP);
    INSTRUMENTS.add(PAYER_SWAP);
    INSTRUMENTS.add(RECEIVER_SWAP);
    INSTRUMENTS.add(PAYER_SWAP_WITH_SPREAD);
    INSTRUMENTS.add(RECEIVER_SWAP_WITH_SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument1() {
    CALCULATOR.getCashFlows(null, TODAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument2() {
    CALCULATOR.getCashFlows(null, IBOR_FIXING_SERIES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate1() {
    CALCULATOR.getCashFlows(PAY_CASH, IBOR_FIXING_SERIES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate2() {
    CALCULATOR.getCashFlows(PAY_CASH, IBOR_FIXING_SERIES, null);
  }

  @Test
  public void testDateBeforeAnyCashFlows() {
    final LocalDate date = LocalDate.of(1990, 1, 1);
    for (final InstrumentDefinition<?> definition : INSTRUMENTS) {
      final TreeMap<LocalDate, MultipleCurrencyAmount> allFlows = new TreeMap<>(definition.accept(ALL_CASH_FLOWS, IBOR_FIXING_SERIES));
      final Map<LocalDate, MultipleCurrencyAmount> trimmed = CALCULATOR.getCashFlows(definition, IBOR_FIXING_SERIES, date);
      assertEquals(allFlows, trimmed);
    }
    for (final InstrumentDefinition<?> definition : NON_FIXING_INSTRUMENTS) {
      final TreeMap<LocalDate, MultipleCurrencyAmount> allFlows = new TreeMap<>(definition.accept(ALL_CASH_FLOWS));
      final Map<LocalDate, MultipleCurrencyAmount> trimmed = CALCULATOR.getCashFlows(definition, date);
      assertEquals(allFlows, trimmed);
    }
  }

  @Test
  public void testDateAfterAnyCashFlows() {
    final LocalDate date = LocalDate.of(2100, 1, 1);
    for (final InstrumentDefinition<?> definition : INSTRUMENTS) {
      final Map<LocalDate, MultipleCurrencyAmount> trimmed = CALCULATOR.getCashFlows(definition, IBOR_FIXING_SERIES, date);
      assertEquals(Collections.emptyMap(), trimmed);
    }
    for (final InstrumentDefinition<?> definition : NON_FIXING_INSTRUMENTS) {
      final Map<LocalDate, MultipleCurrencyAmount> trimmed = CALCULATOR.getCashFlows(definition, date);
      assertEquals(Collections.emptyMap(), trimmed);
    }
  }

  @Test
  public void testTrimming() {
    final Set<InstrumentDefinition<?>> allInstruments = new HashSet<>(INSTRUMENTS);
    allInstruments.addAll(NON_FIXING_INSTRUMENTS);
    final LocalDate date = LocalDate.of(2012, 1, 1);
    for (final InstrumentDefinition<?> definition : allInstruments) {
      final TreeMap<LocalDate, MultipleCurrencyAmount> allFlows = new TreeMap<>(definition.accept(ALL_CASH_FLOWS, IBOR_FIXING_SERIES));
      final Map<LocalDate, MultipleCurrencyAmount> trimmed = CALCULATOR.getCashFlows(definition, IBOR_FIXING_SERIES, date);
      final Iterator<Map.Entry<LocalDate, MultipleCurrencyAmount>> iterator = trimmed.entrySet().iterator();
      for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : allFlows.entrySet()) {
        if (entry.getKey().isBefore(date)) {
          assertFalse(trimmed.containsKey(entry.getKey()));
        } else {
          final Map.Entry<LocalDate, MultipleCurrencyAmount> other = iterator.next();
          assertEquals(entry, other);
        }
      }
      assertFalse(iterator.hasNext());
    }
  }
}
