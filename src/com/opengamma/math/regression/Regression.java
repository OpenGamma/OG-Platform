package com.opengamma.math.regression;

/**
 * 
 * @author emcleod
 * 
 */

public interface Regression {

  public double[] getBetas();

  public double[] getResiduals();
}
