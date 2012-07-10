/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BlackVolatilitySurfaceMoneynessFormatter extends NoHistoryFormatter<BlackVolatilitySurfaceMoneyness> {

  private static final Logger s_logger = LoggerFactory.getLogger(BlackVolatilitySurfaceMoneynessFormatter.class);

  @Override
  public String formatForDisplay(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
    int xCount;
    int yCount;
    if (value.getSurface() instanceof InterpolatedDoublesSurface) {
      InterpolatedDoublesSurface interpolated = (InterpolatedDoublesSurface) value.getSurface();
      xCount = interpolated.getXData().length;
      yCount = interpolated.getYData().length;
    } else if (value.getSurface() instanceof FunctionalDoublesSurface) {
      xCount = 20;
      yCount = 20;
    } else {
      s_logger.warn("Unable for format surface of type {}", value.getSurface().getClass());
      return null;
    }
    return "Volatility Surface (" + xCount + " x " + yCount + ")";
  }

  @Override
  public Object formatForExpandedDisplay(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.SURFACE_DATA;
  }
}
