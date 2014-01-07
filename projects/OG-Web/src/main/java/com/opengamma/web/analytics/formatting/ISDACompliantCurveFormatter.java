/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for IASDACompliant curves.
 */
class ISDACompliantCurveFormatter extends AbstractFormatter<ISDACompliantCurve> {

  /**
   * Creates an instance.
   */
  ISDACompliantCurveFormatter() {
    super(ISDACompliantCurve.class);
    addFormatter(new Formatter<ISDACompliantCurve>(Format.EXPANDED) {
      @Override
      Object format(ISDACompliantCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public List<Double[]> formatCell(ISDACompliantCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<>();
    double[] xData = value.getT();
    double[] yData = value.getKnotZeroRates();
    for (int i = 0; i < xData.length; i++) {
      data.add(new Double[] {xData[i], yData[i] });
    }
    return data;
  }

  // This should really interpolate the curve
  private List<Double[]> formatExpanded(ISDACompliantCurve value) {
    return formatCell(value, null, null);
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }

}
