/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;

/**
 * Class describing the times and amounts required to price interest rate derivatives and the derivative of the amounts to the curves.
 */
public class DecisionScheduleDerivative extends DecisionSchedule {

  /**
   * Derivative of the cash flow equivalent for each decision time.
   */
  private final List<Map<Double, InterestRateCurveSensitivity>> _impactAmountDerivative;

  /**
   * Constructor.
   * @param decisionTime The time at which an exercise or fixing take place.
   * @param impactTime The time impacting the value at each decision date.
   * @param impactAmount The reference amounts at each impact times.
   */
  public DecisionScheduleDerivative(final double[] decisionTime, final double[][] impactTime, final double[][] impactAmount) {
    super(decisionTime, impactTime, impactAmount);
    _impactAmountDerivative = new ArrayList<>();
  }

  /**
   * Constructor.
   * @param decisionTime The time at which an exercise or fixing take place.
   * @param impactTime The time impacting the value at each decision date.
   * @param impactAmount The reference amounts at each impact times.
   * @param impactAmountDerivative Derivative of the cash flow equivalent for each decision time.
   */
  public DecisionScheduleDerivative(final double[] decisionTime, final double[][] impactTime, final double[][] impactAmount,
      final List<Map<Double, InterestRateCurveSensitivity>> impactAmountDerivative) {
    super(decisionTime, impactTime, impactAmount);
    _impactAmountDerivative = impactAmountDerivative;
  }

  /**
   * Gets the derivative of the cash flow equivalent for each decision time.
   * @return The derivative of the cash flow equivalent for each decision time.
   */
  public List<Map<Double, InterestRateCurveSensitivity>> getImpactAmountDerivative() {
    return _impactAmountDerivative;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _impactAmountDerivative.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof DecisionScheduleDerivative)) {
      return false;
    }
    final DecisionScheduleDerivative other = (DecisionScheduleDerivative) obj;
    if (!ObjectUtils.equals(_impactAmountDerivative, other._impactAmountDerivative)) {
      return false;
    }
    return true;
  }

  //  /**
  //   * Sets the derivative of the cash flow equivalent for each decision time.
  //   * @param impactAmountDerivative  The derivative of the cash flow equivalent for each decision time.
  //   */
  //  public void setImpactAmountDerivative(ArrayList<Map<Double, PresentValueSensitivity>> impactAmountDerivative) {
  //    this._impactAmountDerivative = impactAmountDerivative;
  //  }

}
