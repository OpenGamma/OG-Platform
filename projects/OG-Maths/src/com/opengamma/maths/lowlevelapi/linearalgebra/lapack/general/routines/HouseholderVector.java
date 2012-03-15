/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.routines;

/**
 * 
 */
public class HouseholderVector {

  private double _h[];
  private double _beta;

  /**
   * 
   * @param h a
   * @param beta a
   */
  public HouseholderVector(double[] h, double beta) {
    _h = h;
    _beta = beta;
  }

  public double[] getH() {
    return _h;
  }

  public double getBeta() {
    return _beta;
  }

}
