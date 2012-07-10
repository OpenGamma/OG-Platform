/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.DoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilitySurfaceFormatter implements Formatter<VolatilitySurface> {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceFormatter.class);

  @Override
  public Object formatForDisplay(VolatilitySurface value, ValueSpecification valueSpec) {
    Surface<Double, Double, Double> inputSurface = value.getSurface();
    if (inputSurface instanceof DoublesSurface) {
      Set<Double> uniqueXValues = Sets.newHashSet(inputSurface.getXData());
      Set<Double> uniqueYValues = Sets.newHashSet(inputSurface.getYData());
      return "Volatility Surface (" + uniqueXValues.size() + " x " + uniqueYValues.size() + ")";
    } else {
      s_logger.warn("Unable to format surface of type {}", inputSurface.getClass());
      return null;
    }
  }

  @Override
  public Object formatForExpandedDisplay(VolatilitySurface value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(VolatilitySurface history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.SURFACE_DATA;
  }
}
