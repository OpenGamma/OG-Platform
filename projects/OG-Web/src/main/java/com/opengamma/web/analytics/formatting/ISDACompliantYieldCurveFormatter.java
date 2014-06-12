/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for yield curves.
 */
class ISDACompliantYieldCurveFormatter extends AbstractFormatter<ISDACompliantYieldCurve> {

  /**
   * Creates an instance.
   */
  ISDACompliantYieldCurveFormatter() {
    super(ISDACompliantYieldCurve.class);
    addFormatter(new Formatter<ISDACompliantYieldCurve>(Format.EXPANDED) {
      @Override
      Object format(ISDACompliantYieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public List<Double[]> formatCell(ISDACompliantYieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<>();
    double[] xData = value.getT();
    double[] yData = value.getKnotZeroRates();
    for (int i = 0; i < xData.length; i++) {
      data.add(new Double[] {xData[i], yData[i] });
    }
    return data;
  }

  // This should really interpolate the curve
  private List<Double[]> formatExpanded(ISDACompliantYieldCurve value) {
    return formatCell(value, null, null);
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }

}
