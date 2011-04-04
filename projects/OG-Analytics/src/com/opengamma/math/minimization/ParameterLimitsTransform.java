/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

/**
 * Interface for objects containing functions that can transform constrained model parameters into unconstrained fitting parameters and vice versa. It also
 * provides functions that will provide the gradient of the functions that perform these transformations
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
   * A function to transform a constrained model parameter to an unconstrained fitting parameter
   * @param x Model parameter 
   * @return Fitting parameter
   */
  double transform(double x);

  /**
   * A function to transform an unconstrained fitting parameter to a constrained model parameter
   * @param y Fitting parameter
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
