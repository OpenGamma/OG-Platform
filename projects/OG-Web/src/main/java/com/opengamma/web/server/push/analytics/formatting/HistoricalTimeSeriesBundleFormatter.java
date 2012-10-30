/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;

public class HistoricalTimeSeriesBundleFormatter extends NoHistoryFormatter<HistoricalTimeSeriesBundle> {

  @Override
  public Object formatForDisplay(HistoricalTimeSeriesBundle bundle, ValueSpecification valueSpec) {
    return "Time-series Bundle";
  }

  @Override
  public Object formatForExpandedDisplay(HistoricalTimeSeriesBundle value, ValueSpecification valueSpec) {
    // TODO format as matrix 1D, labels are fields, values are lists of external IDs. data not currently exposed by bundle
    throw new UnsupportedOperationException("Expanded display not supported for HistoricalTimeSeriesBundleFormatter");
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
