/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

/**
 * 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public interface Curve<T extends Comparable<T>, U> {

  T[] getXData();

  U[] getYData();

  int size();

  String getName();

  U getYValue(T x);

}
