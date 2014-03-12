/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

/**
 * Interface glue
 */
public interface DoubleMatrix extends Matrix<Double> {

  /**
   * Gets the underlying data as an array of arrays 
   * @return the underlying data as an array of arrays 
   */
  public double [][] asDoubleAoA();

  /**
   * Gets the underlying data as a column major vector
   * @return the underlying data as a column major vector
   */
   public double [] asDoubleArray();
  
}
