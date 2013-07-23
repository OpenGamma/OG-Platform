/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveNodeConverter {

  /**
   * Given an {@link InstrumentDefinition} (the time-independent form used in the analytics library) and a valuation time, converts to the
   * time-dependent {@link InstrumentDerivative} form.
   * @param node The curve node, not null
   * @param definition The definition, not null
   * @param now The valuation time, not null
   * @param timeSeries A fixing time series, not null if required
   * @return A derivative instrument
   */
  @SuppressWarnings("unchecked")
  public static InstrumentDerivative getDerivative(final CurveNodeWithIdentifier node, final InstrumentDefinition<?> definition, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(now, "now");
    if (definition instanceof InstrumentDefinitionWithData<?, ?> && requiresFixingSeries(node.getCurveNode())) {
      if (node.getCurveNode() instanceof RateFutureNode) {
        ArgumentChecker.notNull(timeSeries, "time series");
        final ExternalId id = node.getIdentifier();
        final DoubleTimeSeries<?> ts = timeSeries.get(node.getDataField(), id).getTimeSeries();
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + id);
        }
        final int length = ts.size();
        if (length == 0) {
          throw new OpenGammaRuntimeException("Price time series for " + id + " was empty");
        }
        final double lastMarginPrice = ts.getLatestValue();
        return ((InstrumentDefinitionWithData<?, Double>) definition).toDerivative(now, lastMarginPrice, new String[] {"", "", ""});
      }
      throw new OpenGammaRuntimeException("Cannot handle swaps with fixings");
    }
    return definition.toDerivative(now, new String[] {"", "", ""});
  }

  private static boolean requiresFixingSeries(final CurveNode node) {
    return node instanceof RateFutureNode; // || (node instanceof SwapNode && ((SwapNode) node).isUseFixings());
  }
}
