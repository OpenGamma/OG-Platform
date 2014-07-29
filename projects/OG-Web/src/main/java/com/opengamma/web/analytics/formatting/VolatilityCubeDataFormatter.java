/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for {@code VolatilityCubeData}.
 */
@SuppressWarnings("rawtypes")
/* package */class VolatilityCubeDataFormatter extends AbstractFormatter<VolatilityCubeData> {

  /* package */VolatilityCubeDataFormatter() {
    super(VolatilityCubeData.class);
  }

  @Override
  public String formatCell(final VolatilityCubeData value, final ValueSpecification valueSpec, Object inlineKey) {
    return "Volatility Cube data (" + value.size() + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
