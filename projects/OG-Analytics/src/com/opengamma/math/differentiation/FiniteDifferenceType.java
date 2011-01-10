/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

/**
 * Differencing type:
 * <p>
 * Forward: [f(x + eps) - f(x)] / eps
 * <p>
 * Central: [f(x + eps) - f(x - eps)] / (2 * eps)
 * <p>
 * Backward: [f(x) - f(x - eps)] / eps
 */
public enum FiniteDifferenceType {
  /**
   * Forward differencing
   */
  FORWARD,
  /**
   * Central differencing
   */
  CENTRAL,
  /**
   * Backward differencing
   */
  BACKWARD
}
