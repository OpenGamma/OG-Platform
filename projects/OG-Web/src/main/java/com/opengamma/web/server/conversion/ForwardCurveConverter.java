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

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ForwardCurveConverter implements ResultConverter<ForwardCurve> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, ForwardCurve value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (value.getForwardCurve() instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getForwardCurve();
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
    if (value.getForwardCurve() instanceof FunctionalDoublesCurve) {
      FunctionalDoublesCurve functionalCurve = (FunctionalDoublesCurve) value.getForwardCurve();
      List<Double[]> data = new ArrayList<Double[]>();
      for (int i = 0; i < 30; i++) {
        double x = i;
        data.add(new Double[] {x, functionalCurve.getYValue(x)});
      }
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        List<Double[]> detailedData = getData(functionalCurve);
        result.put("detailed", detailedData);
      }
      return result;
    }
    result.put("summary", "Can only display InterpolatedDoublesCurve or FunctionalDoublesCurve");
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, ForwardCurve value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, ForwardCurve value) {
    if (value.getForwardCurve() instanceof InterpolatedDoublesCurve) {
      StringBuilder sb = new StringBuilder();
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getForwardCurve();
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

  private List<Double[]> getData(InterpolatedDoublesCurve detailedCurve) {
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
  
  private List<Double[]> getData(FunctionalDoublesCurve detailedCurve) {
    List<Double[]> detailedData = new ArrayList<Double[]>();
        
    for (int i = 0; i < 100; i++) {
      double x = 3 * i / 10.;
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
    }
    return detailedData;
  }
}
