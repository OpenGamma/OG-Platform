/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

/**
 * 
 * @param <S>
 * @param <T>
 */
public interface GeneratingFunction<S, T> {

  T generate(int n, S... parameters);
}
