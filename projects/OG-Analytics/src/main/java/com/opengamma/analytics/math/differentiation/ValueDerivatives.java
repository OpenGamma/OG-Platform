/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

/**
 * A value and its derivatives.
 * <p>
 * This defines a standard way to return a value and its derivatives to certain inputs.
 * It is in particular used as a return object for Algorithmic Differentiation versions of some functions.
 */
public final class ValueDerivatives {

  /**
   * The value of the variable.
   */
  private final double _value;
  /**
   * The derivatives of the variable with respect to some inputs.
   */
  private final double[] _derivatives;
  
  /**
   * Constructor.
   * @param value  The function value.
   * @param derivatives  The derivative vector.
   */
  public ValueDerivatives(double value, double[] derivatives) {
    this._value = value;
    this._derivatives = derivatives;
  }

  /**
   * Obtains an instance from a value and array of derivatives.
   * 
   * @param value  the value
   * @param derivatives  the derivatives of the value
   * @return the object
   */
  public static ValueDerivatives of(double value, double[] derivatives) {
    return new ValueDerivatives(value, derivatives);
  }
  
  /**
   * Returns the value.
   * @return the value
   */
  public double getValue() {
    return _value;
  }

  /**
   * Returns the derivatives vector.
   * @return the derivatives
   */
  public double[] getDerivatives() {
    return _derivatives;
  }

  @Override
  public String toString() {
    String s = "[" + _value + ", [";
    for (int i = 0; i < _derivatives.length; i++) {
      s += _derivatives[i] + ", ";
    }
    return s + "]";
  }

}
