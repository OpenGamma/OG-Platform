/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;

public class VolatilitySurfaceSpecificationFormatter extends AbstractFormatter<VolatilitySurfaceSpecification> {

  /* package */ VolatilitySurfaceSpecificationFormatter() {
    super(VolatilitySurfaceSpecification.class);
  }

  @Override
  public Object formatCell(VolatilitySurfaceSpecification spec, ValueSpecification valueSpec) {
    return "Volatility Surface Spec - " + spec.getName() + "/" + spec.getSurfaceQuoteType() + "/" + spec.getQuoteUnits();
  }

  @Override
  public DataType getDataType() {
    return DataType.PRIMITIVE;
  }
}
