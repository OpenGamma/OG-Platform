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

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.math.curve.DoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.curve.NodalDoublesCurve;

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
      for (int i = 0; i < 30; i++) {
        double x = i;
        data.add(new Double[] {x, nodalCurve.getYValue(x)});
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        List<Double[]> detailedData = getData(nodalCurve);
        result.put("detailed", detailedData);
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
    } else if (value instanceof NodalDoublesCurve) {
      StringBuilder sb = new StringBuilder();
      NodalDoublesCurve nodalCurve = (NodalDoublesCurve) value;
      double[] xData = nodalCurve.getXDataAsPrimitive();
      double[] yData = nodalCurve.getYDataAsPrimitive();
      boolean isFirst = true;
      for (int i = 0; i < nodalCurve.size(); i++) {
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
    double x = 0;
    for (int i = 0; i < 100; i++) {      
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

  private List<Double[]> getData(final NodalDoublesCurve detailedCurve) {
    List<Double[]> detailedData = new ArrayList<Double[]>();
    
    Double[] xs = detailedCurve.getXData();
    double eps = (xs[xs.length - 1] - xs[0]) / 100;
    double x = 0;
    for (int i = 0; i < 100; i++) {      
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

}
