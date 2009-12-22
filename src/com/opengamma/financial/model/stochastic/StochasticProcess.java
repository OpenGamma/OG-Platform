/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.stochastic;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 * 
 */
public abstract class StochasticProcess<T, U> {

  public Double[] getPath(final T t, final U u, final Double[] random) {
    final int n = random.length;
    final Function1D<Double, Double> f1 = getPathGeneratingFunction(t, u, n);
    final Function2D<Double, Double> f2 = getPathAccumulationFunction();
    final Double[] path = new Double[n];
    path[0] = f2.evaluate(getInitialValue(t, u), f1.evaluate(random[0]));
    for (int i = 1; i < n; i++) {
      path[i] = f2.evaluate(path[i - 1], f1.evaluate(random[i]));
    }
    return path;
  }

  public List<Double[]> getPaths(final T t, final U u, final List<Double[]> random) {
    final int n = random.size();
    final List<Double[]> paths = new ArrayList<Double[]>(n);
    for (final Double[] r : random) {
      paths.add(getPath(t, u, r));
    }
    return paths;
  }

  public abstract Double getFinalValue(Double x);

  public abstract Double getInitialValue(T t, U u);

  public abstract Function1D<Double, Double> getPathGeneratingFunction(T t, U u, int steps);

  public abstract Function2D<Double, Double> getPathAccumulationFunction();
}
