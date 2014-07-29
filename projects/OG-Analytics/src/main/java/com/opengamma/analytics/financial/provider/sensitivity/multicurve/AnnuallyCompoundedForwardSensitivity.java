/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

/**
 * 
 */
public class AnnuallyCompoundedForwardSensitivity extends ForwardSensitivity {

  public AnnuallyCompoundedForwardSensitivity(final double startTime, final double endTime, final double accrualFactor, final double value) {
    super(startTime, endTime, accrualFactor, value);
  }

  @Override
  public double derivativeToYieldStart(final double dicountfactorStart, final double dicountfactorEnd) {
    return -getStartTime() / getAccrualFactor() * Math.pow(dicountfactorStart / dicountfactorEnd, 1 / getAccrualFactor());
  }

  @Override
  public double derivativeToYieldEnd(final double dicountfactorStart, final double dicountfactorEnd) {
    return getEndTime() / getAccrualFactor() * Math.pow(dicountfactorStart / dicountfactorEnd, 1 / getAccrualFactor());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AnnuallyCompoundedForwardSensitivity []";
  }

}
