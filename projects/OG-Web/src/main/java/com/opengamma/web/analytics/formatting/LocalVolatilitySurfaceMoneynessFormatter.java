/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class LocalVolatilitySurfaceMoneynessFormatter extends AbstractFormatter<LocalVolatilitySurfaceMoneyness> {

  /* package */ LocalVolatilitySurfaceMoneynessFormatter() {
    super(LocalVolatilitySurfaceMoneyness.class);
    addFormatter(new Formatter<LocalVolatilitySurfaceMoneyness>(Format.EXPANDED) {
      @Override
      Object format(LocalVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
        return SurfaceFormatterUtils.formatExpanded(value.getSurface());
      }
    });
  }

  @Override
  public Object formatCell(LocalVolatilitySurfaceMoneyness value, ValueSpecification valueSpec) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
