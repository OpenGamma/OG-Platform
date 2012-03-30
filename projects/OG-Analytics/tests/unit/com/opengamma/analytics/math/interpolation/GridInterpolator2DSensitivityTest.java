/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class GridInterpolator2DSensitivityTest {
  private static final Interpolator1D LINEAR_1D = new LinearInterpolator1D();
  private static final GridInterpolator2D INTERPOLATOR;
  private static final Map<DoublesPair, Double> DATA;
  private static final Map<Double, Interpolator1DDataBundle> DATA_BUNDLE;
  protected static Function<Double, Double> COS_EXP_FUNCTION = new Function<Double, Double>() {

    @Override
    public Double evaluate(Double... x) {
      return Math.sin(Math.PI * x[0] / 10.0) * Math.exp(-x[1] / 5.);
    }
  };

  static {
    DATA = new HashMap<DoublesPair, Double>();
    for (int i = 0; i < 11; i++) {
      for (int j = 0; j < 11; j++) {
        double x = i;
        double y = j;
        DATA.put(new DoublesPair(x, y), COS_EXP_FUNCTION.evaluate(x, y));
      }
    }

    INTERPOLATOR = new GridInterpolator2D(LINEAR_1D, LINEAR_1D);
    DATA_BUNDLE = INTERPOLATOR.getDataBundle(DATA);
  }

  @Test
  public void test() {
    Map<DoublesPair, Double> res = INTERPOLATOR.getNodeSensitivitiesForValue(DATA_BUNDLE, new DoublesPair(4.5, 7.25));
    assertEquals(0.375, res.get(new DoublesPair(4.0, 7.0)),0.0);
    assertEquals(0.375, res.get(new DoublesPair(5.0, 7.0)),0.0);
    assertEquals(0.125, res.get(new DoublesPair(4.0, 8.0)),0.0);
    assertEquals(0.125, res.get(new DoublesPair(5.0, 8.0)),0.0);
    double sum = 0.0;
    for(Map.Entry<DoublesPair, Double> entry :res.entrySet()) {
      sum += entry.getValue();
    }
    assertEquals(1.0, sum , 0.0);
  }

}
