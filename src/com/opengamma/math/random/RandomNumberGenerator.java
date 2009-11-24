/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.List;

/**
 * 
 * @author emcleod
 */
public interface RandomNumberGenerator {

  public List<Double[]> getVectors(final int dimension, final int n);
}
