/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class ForwardCurveFormatter extends NoHistoryFormatter<ForwardCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(ForwardCurveFormatter.class);

  @Override
  public List<Double[]> formatForDisplay(ForwardCurve value, ValueSpecification valueSpec) {
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

  @Override
  public Object formatForExpandedDisplay(ForwardCurve value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.CURVE;
  }
}
