/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @param <S> The type of the data needed for interpolation in the x-direction
 * @param <T> The type of the data needed for interpolation in the y-direction
 */
public class GridInterpolator2DSensitivity<S extends Interpolator1DDataBundle, T extends Interpolator1DDataBundle> {

  private final Interpolator1D<S> _xInterpolator;
  private final Interpolator1D<T> _yInterpolator;
  private final Interpolator1DNodeSensitivityCalculator<S> _xSensitivity;
  private final Interpolator1DNodeSensitivityCalculator<T> _ySensitivity;

  @SuppressWarnings({"unchecked" })
  public GridInterpolator2DSensitivity(final Interpolator1D<S> xInterpolator, final Interpolator1D<T> yInterpolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
    String xName = Interpolator1DFactory.getInterpolatorName(xInterpolator);
    String yName = Interpolator1DFactory.getInterpolatorName(yInterpolator);
    _xSensitivity = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(xName, false);
    _ySensitivity = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(yName, false);
  }

  public Map<DoublesPair, Double> calculate(final Map<Double, T> dataBundle, final DoublesPair value) {
    Validate.notNull(value);
    Validate.notNull(dataBundle, "data bundle");
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    double[][] temp = new double[dataBundle.size()][];
    int i = 0;
    for (final Map.Entry<Double, T> entry : dataBundle.entrySet()) {
      //this is the sensitivity of the point projected onto a column of y-points to those points 
      temp[i++] = _ySensitivity.calculate(entry.getValue(), value.getSecond());
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    //this is the sensitivity of the point to the points projected onto y columns 
    double[] xSense = _xSensitivity.calculate(_xInterpolator.getDataBundle(xData), value.getKey());
    Validate.isTrue(xSense.length == dataBundle.size());
    Map<DoublesPair, Double> res = new HashMap<DoublesPair, Double>();

    double sense;
    i = 0;
    int j = 0;
    for (final Map.Entry<Double, T> entry : dataBundle.entrySet()) {
      double[] yValues = entry.getValue().getKeys();
      for (j = 0; j < yValues.length; j++) {
        sense = xSense[i] * temp[i][j];
        res.put(new DoublesPair(entry.getKey(), yValues[j]), sense);
      }
      i++;
    }

    return res;
  }
}
