/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public abstract class InterpolatorND implements Interpolator<Map<List<Double>, Double>, List<Double>, Double> {

  public abstract InterpolationResult<Double> interpolate(Map<List<Double>, Double> data, List<Double> value);

  protected void checkData(final Map<List<Double>, Double> data) {
    Validate.notNull(data);
    if (data.size() < 2) {
      throw new IllegalArgumentException("Need at least two points to perform interpolation");
    }
    if (data.containsKey(null)) {
      throw new IllegalArgumentException("Cannot have a null key in the data set");
    }
    if (data.containsValue(null)) {
      throw new IllegalArgumentException("Cannot have a null value in the data set");
    }
  }

  protected int getDimension(final Set<List<Double>> coordinates) {
    final Iterator<List<Double>> iter = coordinates.iterator();
    final int size = iter.next().size();
    while (iter.hasNext()) {
      if (iter.next().size() != size) {
        throw new IllegalArgumentException("Not all coordinates in the data set were of the same dimension");
      }
    }
    return size;
  }

  protected double getRadius(final List<Double> x1, final List<Double> x2) {
    double sum = 0;
    double diff;
    for (int i = 0; i < x1.size(); i++) {
      diff = x1.get(i) - x2.get(i);
      sum += diff * diff;
    }
    return Math.sqrt(sum);
  }
}
