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
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class CurveNodeConverter {

  @SuppressWarnings("unchecked")
  public static InstrumentDerivative getDerivative(final CurveNodeWithIdentifier node, final InstrumentDefinition<?> definition, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    if (definition instanceof InstrumentDefinitionWithData<?, ?> && node.getCurveNode() instanceof RateFutureNode) {
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
    return definition.toDerivative(now, new String[] {"", "", ""});
  }

}
