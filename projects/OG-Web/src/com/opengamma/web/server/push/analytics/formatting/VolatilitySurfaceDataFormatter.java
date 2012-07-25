/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilitySurfaceDataFormatter extends NoHistoryFormatter<VolatilitySurfaceData> {

  @Override
  public String formatForDisplay(VolatilitySurfaceData value, ValueSpecification valueSpec) {
    return "Volatility Surface (" + value.getXs().length + " x " + value.getYs().length + ")";
  }

  @Override
  public Object formatForExpandedDisplay(VolatilitySurfaceData value, ValueSpecification valueSpec) {
    // TODO find out what the UI needs to render the surface
    throw new UnsupportedOperationException();
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.SURFACE_DATA;
  }
}
