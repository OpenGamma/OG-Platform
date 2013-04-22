/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;

/* package */ class InterpolatedYieldCurveSpecificationWithSecuritiesFormatter<T>
    extends AbstractFormatter<InterpolatedYieldCurveSpecificationWithSecurities> {

  /* package */ InterpolatedYieldCurveSpecificationWithSecuritiesFormatter() {
    super(InterpolatedYieldCurveSpecificationWithSecurities.class);
  }

  @Override
  public Object formatCell(InterpolatedYieldCurveSpecificationWithSecurities value,
                           ValueSpecification valueSpec, Object inlineKey) {
    String name;
    if (value.getName() == null) {
      name = value.getCurrency().getCode();
    } else {
      name = value.getName() + "_" + value.getCurrency();
    }
    return "Curve Spec for " + name;
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
