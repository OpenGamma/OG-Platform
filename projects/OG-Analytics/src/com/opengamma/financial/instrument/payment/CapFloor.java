package com.opengamma.financial.instrument.payment;

/**
 * Interface describing a generic cap/floor features.
 */
public interface CapFloor {

  /**
   * Gets the strike.
   * @return The strike
   */
  double geStrike();

  /**
   * Gets the isCap flag.
   * @return Flag indicating a Cap (true) or Floor (false)
   */
  boolean isCap();

  /**
   * The pay-off of the cap/floor.
   * @param fixing The fixing value to compute the pay-off: cap : (fixing-strike)^+ / floor : (strike-fixing)^+
   * @return The pay-off
   */
  double payOff(double fixing);

}
