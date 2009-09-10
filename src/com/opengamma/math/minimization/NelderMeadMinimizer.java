package com.opengamma.math.minimization;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;
import com.opengamma.util.CompareUtils;

/**
 * 
 * @author emcleod
 * 
 */

public class NelderMeadMinimizer implements MultidimensionalMinimizer<Double, MathException> {
  private static final double PERCENT_SHIFT = 1.05;
  private int _maxIter;
  private double _eps;

  public NelderMeadMinimizer() {
    _maxIter = 10000;
    _eps = 1e-9;
  }

  public NelderMeadMinimizer(int maxIter) {
    _maxIter = maxIter;
  }

  public NelderMeadMinimizer(double eps) {
    _eps = eps;
  }

  public NelderMeadMinimizer(int maxIter, double eps) {
    _maxIter = maxIter;
    _eps = eps;
  }

  @Override
  public Double[] minimize(Function<Double, Double, MathException> f, Double[] x) throws ConvergenceException, MathException {
    TreeMap<Double, Double[]> yToX = new TreeMap<Double, Double[]>();
    yToX.put(f.evaluate(x), x);
    for (int i = 0; i < x.length; i++) {
      Double[] newX = new Double[x.length];
      System.arraycopy(x, 0, newX, 0, x.length);
      if (CompareUtils.closeEquals(x[i], 0)) {
        newX[i] = PERCENT_SHIFT - 1;
      } else {
        newX[i] *= PERCENT_SHIFT;
      }
      yToX.put(f.evaluate(newX), newX);
    }
    int i = 0;
    while (i < _maxIter) {
      i++;
      if (Math.abs(yToX.firstKey() - yToX.lastKey()) < _eps) {
        return yToX.firstEntry().getValue();
      }
      performReflection(yToX, f);
    }
    throw new ConvergenceException("Maximum of iterations exceeded - try increasing the number of iterations or decreasing accuracy");
  }

  private void performReflection(TreeMap<Double, Double[]> yToX, Function<Double, Double, MathException> f) throws MathException {
    Iterator<Double[]> iter = yToX.values().iterator();
    int n = yToX.size() - 1;
    Double[] meanX = new Double[n];
    for (int i = 0; i < n; i++) {
      Double[] x = iter.next();
      for (int j = 0; j < x.length; j++) {
        meanX[j] = i == 0 ? x[j] / n : meanX[j] + x[j] / n;
      }
    }
    Double[] reflectionX = arrayCombine(meanX, 2, yToX.lastEntry().getValue(), -1);
    double reflectionY = f.evaluate(reflectionX);
    if (reflectionY >= yToX.firstKey() && reflectionY < yToX.lowerKey(yToX.lastKey())) {
      replaceLast(yToX, reflectionX, reflectionY);
      return;
    }
    performExpansion(yToX, f, meanX, reflectionX, reflectionY);
  }

  private void performExpansion(TreeMap<Double, Double[]> yToX, Function<Double, Double, MathException> f, Double[] meanX, Double[] reflectionX, Double reflectionY)
      throws MathException {
    if (reflectionY < yToX.firstKey()) {
      Double[] expansionX = arrayCombine(reflectionX, 2, meanX, -1);
      Double expansionY = f.evaluate(expansionX);
      if (expansionY < reflectionY) {
        replaceLast(yToX, expansionX, expansionY);
        return;
      }
    }
    performContraction(yToX, f, meanX, reflectionX, reflectionY);
  }

  private void performContraction(TreeMap<Double, Double[]> yToX, Function<Double, Double, MathException> f, Double[] meanX, Double[] reflectionX, Double reflectionY)
      throws MathException {
    Double lastY = yToX.lastKey();
    Double secondLastY = yToX.lowerKey(lastY);
    if (reflectionY >= secondLastY && reflectionY < lastY) {
      Double[] outsideContractionX = arrayCombine(reflectionX, 0.5, meanX, 0.5);
      Double outsideContractionY = f.evaluate(outsideContractionX);
      if (outsideContractionY <= reflectionY) {
        replaceLast(yToX, outsideContractionX, outsideContractionY);
        return;
      }
      performShrink(yToX, f);
    } else if (reflectionY >= lastY) {
      Double[] insideContractionX = arrayCombine(meanX, 0.5, yToX.get(lastY), 0.5);
      Double insideContractionY = f.evaluate(insideContractionX);
      if (insideContractionY < lastY) {
        replaceLast(yToX, insideContractionX, insideContractionY);
        return;
      }
    }
    performShrink(yToX, f);
  }

  private void performShrink(TreeMap<Double, Double[]> yToX, Function<Double, Double, MathException> f) throws MathException {
    int n = yToX.size();
    Double[] firstX = yToX.firstEntry().getValue();
    Iterator<Map.Entry<Double, Double[]>> iter = yToX.entrySet().iterator();
    TreeMap<Double, Double[]> result = new TreeMap<Double, Double[]>();
    for (int i = 0; i < n; i++) {
      Map.Entry<Double, Double[]> entry = iter.next();
      if (i == 0) {
        result.put(entry.getKey(), entry.getValue());
      }
      Double[] combined = arrayCombine(firstX, 1, arrayCombine(entry.getValue(), 1, firstX, -1), 0.5);
      result.put(f.evaluate(combined), combined);
    }
    yToX.clear();
    yToX.putAll(result);
  }

  private Double[] arrayCombine(Double[] a, double aScale, Double[] b, double bScale) {
    Double[] result = new Double[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = a[i] * aScale + b[i] * bScale;
    }
    return result;
  }

  private void replaceLast(TreeMap<Double, Double[]> map, Double[] x, Double y) {
    map.remove(map.lastKey());
    map.put(y, x);
  }
}
