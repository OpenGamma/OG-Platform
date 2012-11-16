/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BlackVolatilitySurfaceMoneynessFormatter extends AbstractFormatter<BlackVolatilitySurfaceMoneyness> {

  /* package */ BlackVolatilitySurfaceMoneynessFormatter() {
    super(BlackVolatilitySurfaceMoneyness.class);
    addFormatter(new Formatter<BlackVolatilitySurfaceMoneyness>(Format.EXPANDED) {
      @Override
      Object format(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
        return SurfaceFormatterUtils.formatExpanded(value.getSurface());
      }
    });
  }

  @Override
  public Object formatCell(BlackVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
