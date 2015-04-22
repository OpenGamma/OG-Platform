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

  public abstract double applyShift(double value, double shiftAmount);

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