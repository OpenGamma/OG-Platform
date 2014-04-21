/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

/**
 * 
 */
public class SimplyCompoundedForwardSensitivity extends ForwardSensitivity {

  public SimplyCompoundedForwardSensitivity(final double startTime, final double endTime, final double accrualFactor, final double value) {
    super(startTime, endTime, accrualFactor, value);
  }

  @Override
  public double derivativeToYieldStart(final double dicountfactorStart, final double dicountfactorEnd) {
    return -getStartTime() * dicountfactorStart / (dicountfactorEnd * getAccrualFactor());
  }

  @Override
  public double derivativeToYieldEnd(final double dicountfactorStart, final double dicountfactorEnd) {
    return getEndTime() * dicountfactorStart / (dicountfactorEnd * getAccrualFactor());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SimplyCompoundedForwardSensitivity []";
  }

}
