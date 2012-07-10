/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilityCubeDataFormatter extends NoHistoryFormatter<VolatilityCubeData> {

  @Override
  public String formatForDisplay(VolatilityCubeData value, ValueSpecification valueSpec) {
    return "Volatility Cube data (" + value.getDataPoints().size() + " volatility points, " + value.getATMStrikes().size()
        + " strikes, " + value.getOtherData().getDataPoints().size() + " other data points " + ")";
  }

  @Override
  public Object formatForExpandedDisplay(VolatilityCubeData value, ValueSpecification valueSpec) {
    return formatForDisplay(value, valueSpec);
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.PRIMITIVE;
  }
}
