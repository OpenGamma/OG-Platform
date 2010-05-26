/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

public interface Matrix<T> {
  int getNumberOfElements();

  T getEntry(int... indices);
}
