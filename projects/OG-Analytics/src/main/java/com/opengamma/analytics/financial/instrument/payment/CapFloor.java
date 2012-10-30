/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

/**
 * Interface describing generic cap/floor features.
 */
public interface CapFloor {

  /**
   * Gets the caplet/floorlet strike.
   * @return The strike
   */
  double getStrike();

  /**
   * Gets the cap/floor flag.
   * @return Flag indicating a Cap (true) or Floor (false)
   */
  boolean isCap();

  /**
   * The pay-off of the cap/floor.
   * @param fixing The fixing value to compute the pay-off
   * @return The pay-off.
   */
  double payOff(double fixing);

}
