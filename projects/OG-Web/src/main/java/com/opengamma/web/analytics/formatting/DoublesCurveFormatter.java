/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * TODO make this InterpolatedDoubleCurveFormatter
 */
/* package */ class DoublesCurveFormatter extends AbstractFormatter<DoublesCurve> {

  /* package */ DoublesCurveFormatter() {
    super(DoublesCurve.class);
    addFormatter(new Formatter<DoublesCurve>(Format.EXPANDED) {
      @Override
      Object format(DoublesCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }
  
  @Override
  public Object formatCell(DoublesCurve value, ValueSpecification valueSpec, Object inlineKey) {
    if (value instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      List<Double[]> data = new ArrayList<>();
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    } else if (value instanceof NodalDoublesCurve) {
      NodalDoublesCurve nodalCurve = (NodalDoublesCurve) value;
      List<Double[]> data = new ArrayList<>();
      double[] xData = nodalCurve.getXDataAsPrimitive();
      double[] yData = nodalCurve.getYDataAsPrimitive();
      for (int i = 0; i < nodalCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;      
    } 
    return FORMATTING_ERROR;
  }
  
  private List<Double[]> formatExpanded(DoublesCurve value) {
    NodalDoublesCurve detailedCurve;
    if (value instanceof NodalDoublesCurve) {
      detailedCurve = (NodalDoublesCurve) value;
    } else if (value instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      detailedCurve = NodalDoublesCurve.from(interpolatedCurve.getXDataAsPrimitive(), interpolatedCurve.getYDataAsPrimitive());
    } else {
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + value.getClass());
    }
    List<Double[]> detailedData = new ArrayList<>();
    Double[] xs = detailedCurve.getXData();
    Double[] ys = detailedCurve.getYData();
    for (int i = 0; i < ys.length; i++) {
      detailedData.add(new Double[]{xs[i], ys[i]});
    }
    return detailedData;
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
