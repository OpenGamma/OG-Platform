/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;

/**
 * 
 */
public class BasisSwap implements InterestRateDerivative {

  private final VariableAnnuity _payLeg;
  private final VariableAnnuity _receiveLeg;
  private final FixedAnnuity _spreadLeg;

  public BasisSwap(final VariableAnnuity payLeg, final VariableAnnuity receiveLeg) {
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
    int n = payLeg.getNumberOfPayments();
    double[] coupons = new double[n];
    for (int i = 0; i < n; i++) {
      coupons[i] = 1.0;
    }
    _spreadLeg = new FixedAnnuity(payLeg.getPaymentTimes(), payLeg.getNotional(), coupons, payLeg.getYearFractions(), payLeg.getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_payLeg == null) ? 0 : _payLeg.hashCode());
    result = prime * result + ((_receiveLeg == null) ? 0 : _receiveLeg.hashCode());
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
    BasisSwap other = (BasisSwap) obj;
    if (!ObjectUtils.equals(_payLeg, other._payLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_receiveLeg, other._receiveLeg)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the spreadLeg field.
   * @return the spreadLeg
   */
  public FixedAnnuity getSpreadLeg() {
    return _spreadLeg;
  }

  /**
   * Gets the payLeg field.
   * @return the payLeg
   */
  public VariableAnnuity getPayLeg() {
    return _payLeg;
  }

  /**
   * Gets the reciveLeg field.
   * @return the reciveLeg
   */
  public VariableAnnuity getRecieveLeg() {
    return _receiveLeg;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitBasisSwap(this, curves);
  }

}
