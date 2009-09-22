package com.opengamma.math.rootfinding;

import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 * @author emcleod
 * 
 */

public interface Polynomial1DRootFinder {

  public Double[] getRoot(PolynomialFunction1D polynomial);
}
