/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns the netted results of pay and receive cash-flows, where a negative value implies a net liability.
 */
public final class NettedFixedCashFlowVisitor extends InstrumentDefinitionVisitorSameMethodAdapter<DoubleTimeSeries<LocalDate>, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FixedPayCashFlowVisitor PAY_VISITOR = FixedPayCashFlowVisitor.getInstance();
  private static final FixedReceiveCashFlowVisitor RECEIVE_VISITOR = FixedReceiveCashFlowVisitor.getInstance();
  private static final NettedFixedCashFlowVisitor INSTANCE = new NettedFixedCashFlowVisitor();

  public static InstrumentDefinitionVisitorSameMethodAdapter<DoubleTimeSeries<LocalDate>, Map<LocalDate, MultipleCurrencyAmount>> getVisitor() {
    return INSTANCE;
  }

  /**
   * Returns netted known cash-flows, including any floating cash-flows that have fixed.
   * @param instrument The instrument, not null
   * @return A map containing netted cash-flows.
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> instrument) {
    return visit(instrument, null);
  }

  /**
   * Returns netted known cash-flows, including any floating cash-flows that have fixed.
   * @param instrument The instrument, not null
   * @param indexFixingTimeSeries The fixing time series
   * @return A map containing netted cash-flows.
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> instrument, final DoubleTimeSeries<LocalDate> indexFixingTimeSeries) {
    ArgumentChecker.notNull(instrument, "instrument");
    final Map<LocalDate, MultipleCurrencyAmount> payCashFlows = instrument.accept(PAY_VISITOR, indexFixingTimeSeries);
    final Map<LocalDate, MultipleCurrencyAmount> receiveCashFlows = instrument.accept(RECEIVE_VISITOR, indexFixingTimeSeries);
    return add(payCashFlows, receiveCashFlows);
  }

  private static Map<LocalDate, MultipleCurrencyAmount> add(final Map<LocalDate, MultipleCurrencyAmount> payCashFlows,
      final Map<LocalDate, MultipleCurrencyAmount> receiveCashFlows) {
    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<>(receiveCashFlows);
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payCashFlows.entrySet()) {
      final MultipleCurrencyAmount mca = entry.getValue().multipliedBy(-1);
      final LocalDate date = entry.getKey();
      if (result.containsKey(date)) {
        result.put(date, result.get(date).plus(mca));
      } else {
        result.put(date, mca);
      }
    }
    return result;
  }
}
