package com.opengamma.math.integration;

/**
 * 
 * @author emcleod
 * 
 */

public interface GeneratingFunction<S, T, U extends Exception> {

  public T generate(int n, S... params) throws U;
}
