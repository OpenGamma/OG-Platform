/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class CurveConverter implements ResultConverter<DoublesCurve> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value, final ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (value instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      List<Double[]> data = new ArrayList<Double[]>();
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        List<Double[]> detailedData = getData(interpolatedCurve);
        result.put("detailed", detailedData);
      }
      return result;
    }
    if (value instanceof NodalDoublesCurve) {
      NodalDoublesCurve nodalCurve = (NodalDoublesCurve) value;
      List<Double[]> data = new ArrayList<Double[]>();
      double[] xData = nodalCurve.getXDataAsPrimitive();
      double[] yData = nodalCurve.getYDataAsPrimitive();
      for (int i = 0; i < nodalCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i] });
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        // can't interpolate values for nodal curve, thus summary == detailed
        result.put("detailed", data);
      }
      return result;
    }
    result.put("summary", "Can only display InterpolatedDoublesCurve or NodalDoublesCurve");
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final DoublesCurve value) {
    if (value instanceof InterpolatedDoublesCurve) {
      StringBuilder sb = new StringBuilder();
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value;
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      boolean isFirst = true;
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        if (isFirst) {
          isFirst = false;
        } else {
          sb.append("; ");
        }
        sb.append(xData[i]).append("=").append(yData[i]);
      }
      return sb.length() > 0 ? sb.toString() : null;
    } else {
      return value.getClass().getSimpleName();
    }
  }

  @Override
  public String getFormatterName() {
    return "CURVE";
  }

  private List<Double[]> getData(final InterpolatedDoublesCurve detailedCurve) {
    List<Double[]> detailedData = new ArrayList<Double[]>();
    
    Double[] xs = detailedCurve.getXData();
    double eps = (xs[xs.length - 1] - xs[0]) / 100;
    double x = xs[0];    
    for (int i = 0; i < 100; i++) {      
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

}
