/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BlackVolatilitySurfaceMoneynessFormatter extends AbstractFormatter<BlackVolatilitySurfaceMoneyness> {

  /* package */ BlackVolatilitySurfaceMoneynessFormatter() {
    super(BlackVolatilitySurfaceMoneyness.class);
    addFormatter(new Formatter<BlackVolatilitySurfaceMoneyness>(Format.EXPANDED) {
      @Override
      Object format(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec, Object inlineKey) {
        return SurfaceFormatterUtils.formatExpanded(value.getSurface());
      }
    });
  }

  @Override
  public Object formatCell(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec, Object inlineKey) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  /**
   * Returns {@link DataType#UNKNOWN UNKNOWN} because the type can be differ for different instances depending on
   * the type returned by {@link VolatilitySurface#getSurface() getSurface()}.
   * The type for a given surface instance can be obtained from {@link #getDataTypeForValue}
   * @return {@link DataType#UNKNOWN}
   */
  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }

  @Override
  public DataType getDataTypeForValue(BlackVolatilitySurfaceMoneyness value) {
    return SurfaceFormatterUtils.getDataType(value.getSurface());
  }
}
