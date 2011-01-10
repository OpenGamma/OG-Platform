/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
