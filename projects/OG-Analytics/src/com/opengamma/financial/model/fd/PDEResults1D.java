/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.fd;

/**
 * 
 */
public class PDEResults1D {

  private double[] _f;
  //private double[] _x;

  double getFunctionValue(int index) {
    return _f[index];
  }

}
