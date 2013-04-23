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

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class ForwardCurveFormatter extends AbstractFormatter<ForwardCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(ForwardCurveFormatter.class);

  /* package */ ForwardCurveFormatter() {
    super(ForwardCurve.class);
    addFormatter(new Formatter<ForwardCurve>(Format.EXPANDED) {
      @Override
      Object format(ForwardCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(ForwardCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<Double[]>();
    if (value.getForwardCurve() instanceof InterpolatedDoublesCurve) {
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getForwardCurve();
      double[] xData = interpolatedCurve.getXDataAsPrimitive();
      double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      return data;
    } else if (value.getForwardCurve() instanceof FunctionalDoublesCurve) {
      FunctionalDoublesCurve functionalCurve = (FunctionalDoublesCurve) value.getForwardCurve();
      for (int i = 0; i < 30; i++) {
        double x = i;
        data.add(new Double[] {x, functionalCurve.getYValue(x)});
      }
      return data;
    } else {
      s_logger.warn("Can't format forward curve of type {}", value.getForwardCurve().getClass());
      return null;
    }
  }

  private List<Double[]> formatExpanded(ForwardCurve value) {
    Curve<Double, Double> forwardCurve = value.getForwardCurve();
    if (forwardCurve instanceof FunctionalDoublesCurve) {
      return formatFunctionalDoubleCurve((FunctionalDoublesCurve) forwardCurve);
    } else if (forwardCurve instanceof InterpolatedDoublesCurve) {
      return formatInterpolatedDoubleCurve((InterpolatedDoublesCurve) forwardCurve);
    }
    throw new IllegalArgumentException("Unable to format forward curve of type " + forwardCurve.getClass().getName());
  }

  private List<Double[]> formatInterpolatedDoubleCurve(InterpolatedDoublesCurve detailedCurve) {
    List<Double[]> detailedData = Lists.newArrayList();
    Double[] xs = detailedCurve.getXData();
    double eps = (xs[xs.length - 1] - xs[0]) / 100;
    double x = 0;
    for (int i = 0; i < 100; i++) {
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
      x += eps;
    }
    return detailedData;
  }

  private List<Double[]> formatFunctionalDoubleCurve(FunctionalDoublesCurve detailedCurve) {
    List<Double[]> detailedData = Lists.newArrayList();
    for (int i = 0; i < 100; i++) {
      double x = 3 * i / 10.;
      detailedData.add(new Double[]{x, detailedCurve.getYValue(x)});
    }
    return detailedData;
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
