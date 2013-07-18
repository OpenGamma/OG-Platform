/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.DoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilitySurfaceFormatter extends AbstractFormatter<VolatilitySurface> {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceFormatter.class);

  /* package */ VolatilitySurfaceFormatter() {
    super(VolatilitySurface.class);
    addFormatter(new Formatter<VolatilitySurface>(Format.EXPANDED) {
      @Override
      Object format(VolatilitySurface value, ValueSpecification valueSpec, Object inlineKey) {
        return SurfaceFormatterUtils.formatExpanded(value.getSurface());
      }
    });
  }

  @Override
  public String formatCell(VolatilitySurface value, ValueSpecification valueSpec, Object inlineKey) {
    Surface<Double, Double, Double> inputSurface = value.getSurface();
    if (inputSurface instanceof DoublesSurface) {
      Set<Double> uniqueXValues = Sets.newHashSet(inputSurface.getXData());
      Set<Double> uniqueYValues = Sets.newHashSet(inputSurface.getYData());
      return "Volatility Surface (" + uniqueXValues.size() + " x " + uniqueYValues.size() + ")";
    } else if (inputSurface instanceof ConstantDoublesSurface) {
      return "Constant Volatility Surface (z = " + inputSurface.getZValue(0.0, 0.0) + ")";
    } else {
      s_logger.warn("Unable to format surface of type {}", inputSurface.getClass());
      return null;
    }
  }

  /**
   * Returns {@link DataType#UNKNOWN UNKNOWN} because the type can differ for different instances of
   * {@link VolatilitySurface} depending on the type returned by {@link VolatilitySurface#getSurface() getSurface()}.
   * The type for a given surface instance can be obtained from {@link #getDataTypeForValue}
   * @return {@link DataType#UNKNOWN}
   */
  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }

  @Override
  public DataType getDataTypeForValue(VolatilitySurface value) {
    return SurfaceFormatterUtils.getDataType(value.getSurface());
  }
}
