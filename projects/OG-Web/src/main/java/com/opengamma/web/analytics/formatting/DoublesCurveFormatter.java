/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * TODO make this InterpolatedDoubleCurveFormatter
 */
/* package */ class DoublesCurveFormatter extends AbstractFormatter<DoublesCurve> {

  /* package */ DoublesCurveFormatter() {
    super(DoublesCurve.class);
  }

  @Override
  public Object formatCell(DoublesCurve value, ValueSpecification valueSpec, Object inlineKey) {
    if (value instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      List<Double[]> data = new ArrayList<Double[]>();
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    } else {
      return FORMATTING_ERROR;
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
