/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.array;

import java.util.Collection;


/**
 * A utility class containing static methods for working with arrays.
 */
public final class ArrayUtils {

  private ArrayUtils() {}
  
  /**
   * Returns an array of double primitives with same size and order as
   * the passed collection.
   * 
   * @param doubleList the list of doubles to convert
   * @return the converted double array
   */
  public static double[] toDoubleArray(Collection<Double> doubleList) {
    double[] doubleArray = new double[doubleList.size()];
    
    int i = 0;
    for (Double d : doubleList) {
      doubleArray[i] = d;
      i++;
    }
    
    return doubleArray;
  }
  
}
