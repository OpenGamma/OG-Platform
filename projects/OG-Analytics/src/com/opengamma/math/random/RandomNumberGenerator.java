/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.List;

/**
 * 
 */
public interface RandomNumberGenerator {

  double[] getVector(int dimension);

  List<double[]> getVectors(final int dimension, final int n);
}
