/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;

public class InterpolatedYieldCurveSpecificationWithSecuritiesFormatter<T>
    extends NoHistoryFormatter<InterpolatedYieldCurveSpecificationWithSecurities> {

  @Override
  public Object formatForDisplay(InterpolatedYieldCurveSpecificationWithSecurities value,
                                 ValueSpecification valueSpec) {
    String name;
    if (value.getName() == null) {
      name = value.getCurrency().getCode();
    } else {
      name = value.getName() + "_" + value.getCurrency();
    }
    return "Curve Spec for " + name;
  }

  @Override
  public Object formatForExpandedDisplay(InterpolatedYieldCurveSpecificationWithSecurities value,
                                         ValueSpecification valueSpec) {
    throw new UnsupportedOperationException("Expanded display not supported for " + getClass().getSimpleName());
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
