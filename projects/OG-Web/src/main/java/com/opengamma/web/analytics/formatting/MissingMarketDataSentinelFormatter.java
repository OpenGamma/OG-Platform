/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.cache.MissingMarketDataSentinel;
import com.opengamma.engine.value.ValueSpecification;

/* package */ class MissingMarketDataSentinelFormatter extends AbstractFormatter<MissingMarketDataSentinel> {

  /* package */ MissingMarketDataSentinelFormatter() {
    super(MissingMarketDataSentinel.class);
    addFormatter(new Formatter<MissingMarketDataSentinel>(Format.HISTORY) {
      @Override
      Object format(MissingMarketDataSentinel value, ValueSpecification valueSpec, Object inlineKey) {
        return null;
      }
    });
  }

  @Override
  public Object formatCell(MissingMarketDataSentinel value, ValueSpecification valueSpec, Object inlineKey) {
    return "Missing market data";
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
