/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method.montecarlo;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class DecisionSchedule {

  /**
   * The time at which an exercise or fixing take place.
   */
  private final double[] _decisionTime;
  /**
   * The time impacting the value at each decision date.
   */
  private final double[][] _impactTime;
  /**
   * The reference amounts at each impact times.
   */
  private final double[][] _impactAmount;

  /**
   * Constructor.
   * @param decisionTime The time at which an exercise or fixing take place.
   * @param impactTime The time impacting the value at each decision date.
   * @param impactAmount The reference amounts at each impact times.
   */
  public DecisionSchedule(double[] decisionTime, double[][] impactTime, double[][] impactAmount) {
    Validate.isTrue(decisionTime.length == impactTime.length, "Incorrect length");
    Validate.isTrue(decisionTime.length == impactAmount.length, "Incorrect length");
    _decisionTime = decisionTime;
    _impactTime = impactTime;
    _impactAmount = impactAmount;
  }

  /**
   * Gets the _decisionTime field.
   * @return the _decisionTime
   */
  public double[] getDecisionTime() {
    return _decisionTime;
  }

  /**
   * Gets the _impactTime field.
   * @return the _impactTime
   */
  public double[][] getImpactTime() {
    return _impactTime;
  }

  /**
   * Gets the reference amounts at each impact times.
   * @return the amounts.
   */
  public double[][] getImpactAmount() {
    return _impactAmount;
  }

}
