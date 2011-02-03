/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

/**
 * 
 */
public interface ParameterLimitsTransform {
  /** Types of the limits */
  public enum LimitType {
    /** Greater than limit */
    GREATER_THAN,
    /** Less than limit */
    LESS_THAN
  }

  /**
   * Used to transform from a model parameter that is only allows to take certain values, to a fitting parameter that can take any value
   * @param x Model parameter 
   * @return fitting parameter
   */
  double transform(double x);

  /**
   * Used to transform from a fitting parameter that can take any value, to a model parameter that is only allows to take certain values
   * @param y fitting parameter
   * @return Model parameter 
   */
  double inverseTransform(double y);

  /**
   * The gradient of the function used to transform from a model parameter that is only allows to take certain values, to a fitting parameter that can take any value
   * @param x Model parameter
   * @return the gradient
   */
  double transformGradient(double x);

  /**
   * The gradient of the function used to transform from a fitting parameter that can take any value, to a model parameter that is only allows to take certain values
   * @param y fitting parameter
   * @return the gradient
   */
  double inverseTransformGradient(double y);

}
