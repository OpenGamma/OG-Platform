/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;

/* package */ class HistoricalTimeSeriesBundleFormatter extends AbstractFormatter<HistoricalTimeSeriesBundle> {

  /* package */ HistoricalTimeSeriesBundleFormatter() {
    super(HistoricalTimeSeriesBundle.class);
  }

  @Override
  public Object formatCell(HistoricalTimeSeriesBundle bundle, ValueSpecification valueSpec, Object inlineKey) {
    return "Time-series Bundle";
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
