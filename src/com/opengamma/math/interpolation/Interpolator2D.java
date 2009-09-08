package com.opengamma.math.interpolation;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Interpolator2D {
  protected Double[][] _x;
  protected Double[][] _y;

  public Interpolator2D(Double[][] x, Double[][] y) {
    _x = x;
    _y = y;
  }

  public abstract InterpolationResult interpolate(double x1, double x2) throws InterpolationException;

}
