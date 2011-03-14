/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 */
public abstract class Payment implements InterestRateDerivative {

  private final double _paymentTime;
  private final String _fundingCurveName;

  /**
   * Constructor for a Payment.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   */
  public Payment(double paymentTime, String fundingCurveName) {
    Validate.notNull(fundingCurveName, "funding curve name");
    Validate.isTrue(paymentTime >= 0.0, "payment time < 0");
    _paymentTime = paymentTime;
    _fundingCurveName = fundingCurveName;
  }

  /**
   * Gets the _paymentTime field.
   * @return the _paymentTime
   */
  public double getPaymentTime() {
    return _paymentTime;
  }

  /**
   * Gets the _fundingCurveName field.
   * @return the _fundingCurveName
   */
  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _fundingCurveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    Payment other = (Payment) obj;
    if (!ObjectUtils.equals(_fundingCurveName, other._fundingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    return true;
  }

}
