package com.opengamma.financial.model.volatility.surface;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public class VolatilitySurfaceTransformation {

  public static VolatilitySurface getParallelShiftedSurface(VolatilitySurface original, double shift) {
    Map<Pair<Double, Double>, Double> data = new HashMap<Pair<Double, Double>, Double>();
    for (Map.Entry<Pair<Double, Double>, Double> entry : original.getData().entrySet()) {
      data.put(entry.getKey(), entry.getValue() + shift);
    }
    return new VolatilitySurface(original.getDate(), data, original.getInterpolator());
  }

  public static VolatilitySurface getSingleShiftedDataPointSurface(VolatilitySurface original, int dataIndex, double shift) {
    if (dataIndex < 0)
      throw new IllegalArgumentException("Shift point must be positive");
    SortedMap<Pair<Double, Double>, Double> data = new TreeMap<Pair<Double, Double>, Double>(new FirstThenSecondPairComparator<Double, Double>());
    data.putAll(original.getData());
    int i = 0;
    for (Map.Entry<Pair<Double, Double>, Double> entry : data.entrySet()) {
      if (i++ == dataIndex) {
        data.put(entry.getKey(), entry.getValue() + shift);
      }
    }
    return new VolatilitySurface(original.getDate(), data, original.getInterpolator());
  }

  public static VolatilitySurface getSingleShiftedPointSurface(VolatilitySurface original, Pair<Double, Double> point, double shift) {
    return null;
  }
}
