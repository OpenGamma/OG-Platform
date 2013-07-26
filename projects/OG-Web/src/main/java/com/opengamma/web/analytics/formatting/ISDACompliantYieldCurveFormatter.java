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

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveInterpolatingFunction;

/**
 *
 */
/* package */ class ISDACompliantYieldCurveFormatter extends AbstractFormatter<ISDACompliantYieldCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(ISDACompliantYieldCurveFormatter.class);

  /* package */ ISDACompliantYieldCurveFormatter() {
    super(ISDACompliantYieldCurve.class);
    addFormatter(new Formatter<ISDACompliantYieldCurve>(Format.EXPANDED) {
      @Override
      Object format(ISDACompliantYieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(ISDACompliantYieldCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<>();
      double[] xData = value.getT();
      double[] yData = value.getR();
      for (int i = 0; i < xData.length; i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
    return data;
  }

  // This should really interpolate the curve
  private List<Double[]> formatExpanded(ISDACompliantYieldCurve value) {
    return formatCell(value, null, null);
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
