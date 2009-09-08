package com.opengamma.financial.model.interestrate.curve;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 */

// TODO shit name - change it
public class DiscountCurveTransformation {

  public static DiscountCurve getParallelShiftedCurve(DiscountCurve original, double shift) {
    Map<Double, Double> data = new HashMap<Double, Double>();
    for (Map.Entry<Double, Double> entry : original.getData().entrySet()) {
      data.put(entry.getKey(), entry.getValue() + shift);
    }
    return new DiscountCurve(original.getDate(), data, original.getInterpolator());
  }

  public static DiscountCurve getSingleShiftedDataPointCurve(DiscountCurve original, int dataIndex, double shift) {
    if (dataIndex < 0)
      throw new IllegalArgumentException("Shift point must be positive");
    SortedMap<Double, Double> data = new TreeMap<Double, Double>(original.getData());
    if (dataIndex >= data.size())
      throw new IllegalArgumentException("Could not shift point " + dataIndex + "; number of data points in DiscountCurve is " + data.size());
    int i = 0;
    for (Map.Entry<Double, Double> entry : data.entrySet()) {
      if (i++ == dataIndex) {
        data.put(entry.getKey(), entry.getValue() + shift);
      }
    }
    return new DiscountCurve(original.getDate(), data, original.getInterpolator());
  }

  public static DiscountCurve getSingleShiftedPointCurve(DiscountCurve original, double shiftTime, double shift) {
    if (shiftTime < 0)
      throw new IllegalArgumentException("Shift time must be positive");
    SortedMap<Double, Double> data = new TreeMap<Double, Double>(original.getData());
    if (shiftTime >= data.lastKey())
      throw new IllegalArgumentException("Could not shift at time " + shiftTime + "; last time in DiscountCurve is " + data.lastKey());
    if (data.containsKey(shiftTime)) {
      data.put(shiftTime, data.get(shiftTime) + shift);
    } else {
      try {
        double interpolatedPoint = original.getInterestRate(shiftTime);
        data.put(shiftTime, interpolatedPoint + shift);
      } catch (InterpolationException e) {
        // TODO logging
        return null;
      }
    }
    return new DiscountCurve(original.getDate(), data, original.getInterpolator());
  }

  public static DiscountCurve getMultipleShiftedDataPointCurve(DiscountCurve original, Map<Integer, Double> shifts) {
    if (shifts == null || shifts.isEmpty())
      return original;
    SortedMap<Double, Double> data = new TreeMap<Double, Double>(original.getData());
    int i = 0;
    for (Map.Entry<Double, Double> entry : data.entrySet()) {
      if (shifts.containsKey(i)) {
        data.put(entry.getKey(), entry.getValue() + shifts.get(i));
      }
      i++;
    }
    return new DiscountCurve(original.getDate(), data, original.getInterpolator());
  }

  public static DiscountCurve getMultipleShiftedPointCurve(DiscountCurve original, Map<Double, Double> shifts) {
    if (shifts == null || shifts.isEmpty())
      return original;
    Map<Double, Double> data = new HashMap<Double, Double>(original.getData());
    for (Map.Entry<Double, Double> entry : shifts.entrySet()) {
      if (data.containsKey(entry.getKey())) {
        data.put(entry.getKey(), entry.getValue() + data.get(entry.getKey()));
      } else {
        try {
          Double interpolatedPoint = original.getInterestRate(entry.getKey());
          data.put(entry.getKey(), interpolatedPoint + entry.getValue());
        } catch (InterpolationException e) {
          // TODO logging
          return null;
        }
      }
    }
    return new DiscountCurve(original.getDate(), data, original.getInterpolator());
  }
}
