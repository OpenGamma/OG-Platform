/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class HazardRateCurveFormatter extends AbstractFormatter<HazardRateCurve> {

  /* package */ HazardRateCurveFormatter() {
    super(HazardRateCurve.class);
    addFormatter(new Formatter<HazardRateCurve>(Format.EXPANDED) {
      @Override
      Object format(HazardRateCurve value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(HazardRateCurve value, ValueSpecification valueSpec, Object inlineKey) {
    List<Double[]> data = new ArrayList<>();
    double[] xData = value.getShiftedTimePoints();
    for (int i = 0; i < xData.length; i++) {
      data.add(new Double[] {xData[i], value.getHazardRate(xData[i])});
    }
    return data;
  }

  private List<Double[]> formatExpanded(HazardRateCurve value) {
    List<Double[]> detailedData = new ArrayList<>();
    double[] xs = value.getShiftedTimePoints();
    for (int i = 0; i < xs.length; i++) {
      detailedData.add(new Double[]{xs[i], value.getHazardRate(xs[i])});
    }
    return detailedData;
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
