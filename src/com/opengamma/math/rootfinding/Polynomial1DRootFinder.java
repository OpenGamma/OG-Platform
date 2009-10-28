/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 * @author emcleod
 */
public interface Polynomial1DRootFinder<T> {

  public T[] getRoots(PolynomialFunction1D function);
}
