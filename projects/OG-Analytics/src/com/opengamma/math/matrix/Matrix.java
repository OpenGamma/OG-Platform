/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

/**
 * 
 * @param <T> Type of elements
 */
public interface Matrix<T> {
  int getNumberOfElements();

  T getEntry(int... indices);
}
