/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.stochastic;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;

/**
 * 
 * @param <T>
 * @param <U>
 */
public abstract class StochasticProcess<T, U> {

  public double[] getPath(final T t, final U u, final double[] random) {
    final int n = random.length;
    final Function1D<Double, Double> f1 = getPathGeneratingFunction(t, u, n);
    final Function2D<Double, Double> f2 = getPathAccumulationFunction();
    final double[] path = new double[n];
    path[0] = f2.evaluate(getInitialValue(t, u), f1.evaluate(random[0]));
    for (int i = 1; i < n; i++) {
      path[i] = f2.evaluate(path[i - 1], f1.evaluate(random[i]));
    }
    return path;
  }

  public List<double[]> getPaths(final T t, final U u, final List<double[]> random) {
    final int n = random.size();
    final List<double[]> paths = new ArrayList<>(n);
    for (final double[] r : random) {
      paths.add(getPath(t, u, r));
    }
    return paths;
  }

  public abstract Double getFinalValue(Double x);

  public abstract Double getInitialValue(T t, U u);

  public abstract Function1D<Double, Double> getPathGeneratingFunction(T t, U u, int steps);

  public abstract Function2D<Double, Double> getPathAccumulationFunction();
}
