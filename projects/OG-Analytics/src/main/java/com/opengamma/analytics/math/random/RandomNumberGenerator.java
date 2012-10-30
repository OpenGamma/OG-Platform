/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.random;

import java.util.List;

/**
 * 
 */
public interface RandomNumberGenerator {

  double[] getVector(int dimension);

  List<double[]> getVectors(final int dimension, final int n);
}
