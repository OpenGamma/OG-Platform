/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import org.apache.commons.lang.Validate;

/**
 * A collection of basic useful maths functions 
 */
public abstract class UtilFunctions {

  /**
   * Returns the square of a number
   * @param x some number
   * @return x*x
   */
  public static double square(double x) {
    return x * x;
  }

  /**
  * Returns the cube of a number
   * @param x some number
   * @return x*x*x
   */
  public static double cube(double x) {
    return x * x * x;
  }

  public static int toTenorIndex(final int[] indicies, final int[] dimensions) {
    int dim = indicies.length;
    Validate.isTrue(dim == dimensions.length);
    int sum = 0;
    int product = 1;
    for (int i = 0; i < dim; i++) {
      Validate.isTrue(indicies[i] < dimensions[i], "index out of bounds");
      sum += indicies[i] * product;
      product *= dimensions[i];
    }
    return sum;
  }

  public static int[] fromTenorIndex(final int index, final int[] dimensions) {
    int dim = dimensions.length;
    int[] res = new int[dim];

    int product = 1;
    int[] products = new int[dim - 1];
    for (int i = 0; i < dim - 1; i++) {
      product *= dimensions[i];
      products[i] = product;
    }

    int a = index;
    for (int i = dim - 1; i > 0; i--) {
      res[i] = a / products[i - 1];
      a -= res[i] * products[i - 1];
    }
    res[0] = a;

    return res;
  }

}
