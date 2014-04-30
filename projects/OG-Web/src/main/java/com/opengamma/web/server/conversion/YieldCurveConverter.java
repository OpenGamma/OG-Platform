/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveInterpolatingFunction;

/**
 * Converter for {@link YieldCurve} objects.
 */
public class YieldCurveConverter implements ResultConverter<YieldCurve> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final YieldCurve value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();

    if (value.getCurve() instanceof InterpolatedDoublesCurve) {
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getCurve();
      // Summary view includes only the actual points of the curve
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = interpolatedCurve.getXDataAsPrimitive();
      final double[] yData = interpolatedCurve.getYDataAsPrimitive();
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        data.add(new Double[] {xData[i], yData[i]});
      }
      result.put("summary", data);

      if (mode == ConversionMode.FULL) {
        final NodalDoublesCurve detailedCurve = YieldCurveInterpolatingFunction.interpolateCurve(interpolatedCurve);
        final List<Double[]> detailedData = getData(detailedCurve);
        result.put("detailed", detailedData);
      }
      return result;
    } else if (value.getCurve() instanceof FunctionalDoublesCurve) {
      final FunctionalDoublesCurve curve = (FunctionalDoublesCurve) value.getCurve();
      final int n = 34;
      final List<Double[]> data = new ArrayList<>();
      final double[] xData = new double[n];
      final double[] yData = new double[n];
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
      result.put("summary", data);
      if (mode == ConversionMode.FULL) {
        final NodalDoublesCurve detailedCurve = YieldCurveInterpolatingFunction.interpolateCurve(curve);
        final List<Double[]> detailedData = getData(detailedCurve);
        result.put("detailed", detailedData);
      }
      return result;
    }
    result.put("summary", "Can only display InterpolatedDoublesCurve and FunctionalDoublesCurve");
    return result;
  }

  private List<Double[]> getData(final NodalDoublesCurve detailedCurve) {
    final List<Double[]> detailedData = new ArrayList<>();

    final Double[] xs = detailedCurve.getXData();
    final Double[] ys = detailedCurve.getYData();
    for (int i = 0; i < ys.length; i++) {
      detailedData.add(new Double[]{xs[i], ys[i]});
    }
    return detailedData;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final YieldCurve value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final YieldCurve value) {
    if (value.getCurve() instanceof InterpolatedDoublesCurve) {
      final StringBuilder sb = new StringBuilder();
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) value.getCurve();
      final double[] xData = interpolatedCurve.getXDataAsPrimitive();
      final double[] yData = interpolatedCurve.getYDataAsPrimitive();
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

}
