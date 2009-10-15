/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

/**
 * 
 * @author emcleod
 * 
 */

public interface GeneratingFunction<S, T> {

  public T generate(int n, S... params);
}
