package com.opengamma.math.interpolation;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public class CubicInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException {
    TreeMap<Double, Double> sorted = initData(data);
    int index = getLowerBoundIndex(sorted, value);
    if (index < 2) {
      throw new IllegalArgumentException("Data point was too close to the lowest value of the abscissa data");// TODO
    }
    if (index > sorted.size() - 2) {
      throw new IllegalArgumentException("Data point was too close to the highest value of the abscissa data");// TODO
    }
    Double[] xArray = Arrays.copyOfRange(sorted.keySet().toArray(new Double[0]), index - 1, index + 2);
    Double[] yArray = Arrays.copyOfRange(sorted.values().toArray(new Double[0]), index - 1, index + 2);
    double xDiff01 = xArray[0] - xArray[1];
    double xDiff02 = xArray[0] - xArray[2];
    double xDiff03 = xArray[0] - xArray[3];
    double xDiff12 = xArray[1] - xArray[2];
    double xDiff13 = xArray[1] - xArray[3];
    double xDiff23 = xArray[2] - xArray[3];
    double xDiff0 = value - xArray[0];
    double xDiff1 = value - xArray[1];
    double xDiff2 = value - xArray[2];
    double xDiff3 = value - xArray[3];
    double result = xDiff1 * xDiff2 * xDiff3 * yArray[0] / (xDiff01 * xDiff02 * xDiff03) + xDiff0 * xDiff2 * xDiff3 * yArray[1] / (-xDiff01 * xDiff12 * xDiff13) + xDiff0 * xDiff1
        * xDiff3 * yArray[2] / (xDiff02 * xDiff12 * xDiff23) + xDiff0 * xDiff1 * xDiff2 * yArray[3] / (-xDiff13 * xDiff13 * xDiff23);
    return new InterpolationResult<Double>(result);
  }
}
