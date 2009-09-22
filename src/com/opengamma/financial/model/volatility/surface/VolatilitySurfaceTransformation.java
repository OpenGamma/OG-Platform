/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */

public class VolatilitySurfaceTransformation {
  // TODO these methods should all be functions
  private static final Comparator<Pair<Double, Double>> _firstThenSecondComparator = new FirstThenSecondPairComparator<Double, Double>();

  public static VolatilitySurface getParallelShiftedSurface(VolatilitySurface original, double shift) {
    Map<Pair<Double, Double>, Double> data = new HashMap<Pair<Double, Double>, Double>();
    for (Map.Entry<Pair<Double, Double>, Double> entry : original.getData().entrySet()) {
      data.put(entry.getKey(), entry.getValue() + shift);
    }
    return new VolatilitySurface(original.getDate(), data, original.getInterpolator());
  }

  public static VolatilitySurface getSingleShiftedPointSurface(VolatilitySurface original, Pair<Double, Double> point, double shift) {
    SortedMap<Pair<Double, Double>, Double> data = new TreeMap<Pair<Double, Double>, Double>(_firstThenSecondComparator);
    data.putAll(original.getData());
    if (point.compareTo(data.lastKey()) > 0)
      throw new IllegalArgumentException("Could not shift at point " + point + "; last point in VolatilitySurface is " + data.lastKey());
    if (data.containsKey(point)) {
      data.put(point, data.get(point) + shift);
    } else {
      try {
        double interpolatedPoint = original.getVolatility(point.getFirst(), point.getSecond());
        data.put(point, interpolatedPoint + shift);
      } catch (InterpolationException e) {
        // TODO logging
        return null;
      }
    }
    return new VolatilitySurface(original.getDate(), data, original.getInterpolator());
  }

  public static VolatilitySurface getMultipleShiftedPointSurface(VolatilitySurface original, Map<Pair<Double, Double>, Double> shifts) {
    if (shifts == null || shifts.isEmpty())
      return original;
    Map<Pair<Double, Double>, Double> data = new HashMap<Pair<Double, Double>, Double>();
    for (Map.Entry<Pair<Double, Double>, Double> entry : shifts.entrySet()) {
      Pair<Double, Double> pair = entry.getKey();
      if (data.containsKey(pair)) {
        data.put(pair, entry.getValue() + data.get(pair));
      } else {
        try {
          Double interpolatedPoint = original.getVolatility(pair.getFirst(), pair.getSecond());
          data.put(pair, interpolatedPoint + entry.getValue());
        } catch (InterpolationException e) {
          // TODO logging
          return null;
        }
      }
    }
    return new VolatilitySurface(original.getDate(), data, original.getInterpolator());
  }
}
