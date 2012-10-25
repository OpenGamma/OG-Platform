/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;

public class VolatilitySurfaceSpecificationFormatter extends NoHistoryFormatter<VolatilitySurfaceSpecification> {

  @Override
  public Object formatForDisplay(VolatilitySurfaceSpecification spec, ValueSpecification valueSpec) {
    return "Volatility Surface Spec - " + spec.getName() + "/" + spec.getSurfaceQuoteType() + "/" + spec.getQuoteUnits();
  }

  @Override
  public Object formatForExpandedDisplay(VolatilitySurfaceSpecification value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("Expanded display not supported for " + getClass().getSimpleName());
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
