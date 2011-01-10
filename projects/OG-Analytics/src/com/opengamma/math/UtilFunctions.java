/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

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

}
