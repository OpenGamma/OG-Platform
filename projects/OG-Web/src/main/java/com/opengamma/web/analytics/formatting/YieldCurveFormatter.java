/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveInterpolatingFunction;

/**
 *
 */
/* package */ class YieldCurveFormatter extends AbstractFormatter<YieldCurve> {

  /* package */ YieldCurveFormatter() {
    super(YieldCurve.class);
    addFormatter(new Formatter<YieldCurve>(Format.EXPANDED) {
      @Override
      Object format(YieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(YieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
    if (value.getCurve() instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getCurve();
      List<Double[]> data = new ArrayList<Double[]>();
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    } 
    return getSampledCurve(value.getCurve());
  }

  private List<Double[]> formatExpanded(YieldCurve value) {
    NodalDoublesCurve detailedCurve = YieldCurveInterpolatingFunction.interpolateCurve(value.getCurve());
    List<Double[]> detailedData = new ArrayList<Double[]>();
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
  
  private List<Double[]> getSampledCurve(DoublesCurve curve) {
    int n = 34;
    List<Double[]> data = new ArrayList<Double[]>();
    double[] xData = new double[n];
    double[] yData = new double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        xData[0] = 1. / 12;
      } else if (i == 1) {
        xData[1] = 0.25;
      } else if (i == 2) {
        xData[2] = 0.5;
      } else if (i == 3) {
        xData[3] = 0.75;
      } else {
        xData[i] = i - 3;
      }
      yData[i] = curve.getYValue(xData[i]);
      data.add(new Double[]{xData[i], yData[i]});
    }
    return data;

  }
}
