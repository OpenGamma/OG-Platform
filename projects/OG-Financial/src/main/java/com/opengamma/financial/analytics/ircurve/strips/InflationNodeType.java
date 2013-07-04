/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 *
 */
public enum InflationNodeType {

  /**
   * Indicates monthly zero inflation nodes, where the value at the start of the month are taken.
   */
  MONTHLY,
  /**
   * Indicates interpolated zero inflation nodes, where the value is an interpolation between
   * monthly inflation values.
   */
  INTERPOLATED
}
