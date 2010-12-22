/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class ShepardInterpolatorND extends InterpolatorND {
  private final Function1D<Double, Double> _basisFunction;

  public ShepardInterpolatorND(final double power) {
    _basisFunction = new ShepardNormalizedRadialBasisFunction(power);
  }

  @Override
  public Double interpolate(final Map<List<Double>, Double> data, final List<Double> value) {
    Validate.notNull(value);
    checkData(data);
    final int dimension = getDimension(data.keySet());
    if (value.size() != dimension) {
      throw new IllegalArgumentException("The value has dimension " + value.size() + "; the dimension of the data was " + dimension);
    }
    double sum = 0, weightedSum = 0;
    double r, w;
    final Iterator<Map.Entry<List<Double>, Double>> iter = data.entrySet().iterator();
    Map.Entry<List<Double>, Double> entry;
    while (iter.hasNext()) {
      entry = iter.next();
      r = getRadius(value, entry.getKey());
      if (r == 0) {
        return entry.getValue();

      }
      w = _basisFunction.evaluate(r);
      sum += w;
      weightedSum += w * entry.getValue();
    }
    return weightedSum / sum;
  }
}
