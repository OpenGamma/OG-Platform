/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics;

/**
 * Enum representing data shift types.
 */
public enum ShiftType {

  /**
   * Relative shifts.
   */
  RELATIVE("Relative") {
    @Override
    public double applyShift(double value, double shift) {
      return value * (1 + shift);
    }
  },

  /**
   * Absolute shifts.
   */
  ABSOLUTE("Absolute") {
    @Override
    public double applyShift(double value, double shift) {
      return value + shift;
    }
  };

  /**
   * Apply a absolute or relative shift. An absolute shift adds the shift amount to the rate. Relative shifts apply a
   * scale factor to the input value. e.g. a 10% shift multiplies the rate by 1.1, a -20% shift multiplies the rate
   * by 0.8. So for relative shifts the shifted rate is {@code (rate x (1 + shift))}.
   * @param value the value to shift
   * @param shift the shift to apply
   * @return the shifted value
   */
  public abstract double applyShift(double value, double shift);

  /** The name of the shift type */
  private String _name;

  /**
   * @param name The name
   */
  private ShiftType(final String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return _name;
  }
}
