/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Class describing the time and amount required to price interest rate derivatives. The data is used in particular for Monte Carlo pricing.
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
   * Gets the decision times.
   * @return The decision times.
   */
  public double[] getDecisionTime() {
    return _decisionTime;
  }

  /**
   * Gets the time impacting the value at each decision date.
   * @return The impact times.
   */
  public double[][] getImpactTime() {
    return _impactTime;
  }

  /**
   * Gets the reference amounts at each impact times.
   * @return The amounts.
   */
  public double[][] getImpactAmount() {
    return _impactAmount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_decisionTime);
    result = prime * result + Arrays.hashCode(_impactAmount);
    result = prime * result + Arrays.hashCode(_impactTime);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DecisionSchedule other = (DecisionSchedule) obj;
    if (!Arrays.equals(_decisionTime, other._decisionTime)) {
      return false;
    }
    if (!Arrays.equals(_impactAmount, other._impactAmount)) {
      return false;
    }
    if (!Arrays.equals(_impactTime, other._impactTime)) {
      return false;
    }
    return true;
  }

}
