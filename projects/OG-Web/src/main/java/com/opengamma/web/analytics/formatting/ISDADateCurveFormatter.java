/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveInterpolatingFunction;

/**
 *
 */
/* package */ class ISDADateCurveFormatter extends AbstractFormatter<ISDADateCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(ISDADateCurveFormatter.class);

  /* package */ ISDADateCurveFormatter() {
    super(ISDADateCurve.class);
    addFormatter(new Formatter<ISDADateCurve>(Format.EXPANDED) {
      @Override
      Object format(ISDADateCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(ISDADateCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<>();
    DoublesCurve curve = value.getCurve();
    if (curve instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve;
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < xData.length; i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    }
    s_logger.warn("Unable to format curve of type {}", value.getCurve().getClass());
    return null;
  }

  private List<Double[]> formatExpanded(ISDADateCurve value) {
    NodalDoublesCurve detailedCurve = YieldCurveInterpolatingFunction.interpolateCurve(value.getCurve());
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
