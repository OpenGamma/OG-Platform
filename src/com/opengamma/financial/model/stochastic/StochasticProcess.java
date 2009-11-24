/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.stochastic;

import java.util.List;

/**
 * 
 * @author emcleod
 * 
 */
public interface StochasticProcess<T, U> {

  public List<Double[]> getPath(T t, U u, int n, int steps);
}
