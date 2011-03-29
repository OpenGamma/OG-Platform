package com.opengamma.financial.instrument.payment;

/**
 * Interface describing a generic cap/floor features.
 */
public interface CapFloor {

  /**
   * Gets the _strike field.
   * @return the strike
   */
  double geStrike();

  /**
   * Gets the isCap field.
   * @return the isCap
   */
  boolean isCap();

  /**
   * The pay-off of the cap/floor.
   * @param fixing The fixing value to compute the pay-off: cap : (fixing-strike)^+ / floor : (strike-fixing)^+
   * @return The pay-off
   */
  double payOff(double fixing);
  //    double omega = (_isCap) ? 1 : -1;
  //    return Math.max(omega * (fixing - _strike), 0);

}
