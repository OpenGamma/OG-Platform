package com.opengamma.math.integration;

/**
 * 
 * @author emcleod
 * 
 */

public interface GeneratingFunction<S, T> {

  public T generate(int n, S... params);
}
