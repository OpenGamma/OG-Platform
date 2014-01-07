/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Map;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns a map containing netted known cash-flow dates and MultipleCurrencyAmounts from a particular date.
 */
public final class NettedFixedCashFlowFromDateCalculator {
  private static final NettedFixedCashFlowFromDateCalculator INSTANCE = new NettedFixedCashFlowFromDateCalculator();

  public static NettedFixedCashFlowFromDateCalculator getInstance() {
    return INSTANCE;
  }

  private NettedFixedCashFlowFromDateCalculator() {
  }

  /**
   * Returns the dates and amount of all netted cash-flows of an instrument after a date (inclusive). The map returned is sorted on dates.
   * If there are no cash-flows on or after this date, the result is an empty map.
   * @param instrument The instrument, not null
   * @param date The date from which to calculate fixed cash flows, not null
   * @return A map containing all netted fixed cash-flows from the date (inclusive)
   */
  public Map<LocalDate, MultipleCurrencyAmount> getCashFlows(final InstrumentDefinition<?> instrument, final LocalDate date) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(date, "date");
    final TreeMap<LocalDate, MultipleCurrencyAmount> allFlows = new TreeMap<>(
        instrument.accept(NettedFixedCashFlowVisitor.getVisitor()));
    return allFlows.tailMap(date, true);
  }

  /**
   * Returns the dates and amount of all netted cash-flows of an instrument after a date (inclusive). The map returned is sorted on dates.
   * If there are no cash-flows on or after this date, the result is an empty map.
   * @param instrument The instrument, not null
   * @param fixingSeries The fixing series for the instrument (if required)
   * @param date The date from which to calculate fixed cash flows, not null
   * @return A map containing all netted fixed cash-flows from the date (inclusive)
   */
  public Map<LocalDate, MultipleCurrencyAmount> getCashFlows(final InstrumentDefinition<?> instrument, final DoubleTimeSeries<LocalDate> fixingSeries,
      final LocalDate date) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(date, "date");
    final TreeMap<LocalDate, MultipleCurrencyAmount> allFlows = new TreeMap<>(instrument.accept(
        NettedFixedCashFlowVisitor.getVisitor(), fixingSeries));
    return allFlows.tailMap(date, true);
  }

}
