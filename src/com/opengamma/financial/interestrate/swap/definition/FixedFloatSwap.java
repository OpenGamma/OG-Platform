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
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FixedFloatSwap implements InterestRateDerivative {

  private final FixedAnnuity _fixedLeg;
  private final VariableAnnuity _floatingLeg;

  public FixedFloatSwap(final FixedAnnuity fixedLeg, final VariableAnnuity floatingLeg) {
    Validate.notNull(fixedLeg);
    Validate.notNull(floatingLeg);
    _fixedLeg = fixedLeg;
    _floatingLeg = floatingLeg;
  }

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg) with notional of 1.0
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param floatingPaymentTimes Time in Years of floating payments
   * @param fwdStartOffsets offset in years of start of libor accruing period from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor accruing period from floating payment time
   * @param fundingCurveName Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public FixedFloatSwap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, final double[] fwdStartOffsets, final double[] fwdEndOffsets, String fundingCurveName,
      String liborCurveName) {
    Validate.notNull(fixedPaymentTimes);
    Validate.notNull(floatingPaymentTimes);
    Validate.notNull(fwdStartOffsets);
    Validate.notNull(fwdEndOffsets);
    ArgumentChecker.notEmpty(fixedPaymentTimes, "fixedPaymentTime");
    ArgumentChecker.notEmpty(floatingPaymentTimes, "floatingPaymentTime");
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdEndOffsets");
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);

    _fixedLeg = new FixedAnnuity(fixedPaymentTimes, fundingCurveName);
    _floatingLeg = new VariableAnnuity(floatingPaymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, fundingCurveName, liborCurveName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_fixedLeg == null) ? 0 : _fixedLeg.hashCode());
    result = prime * result + ((_floatingLeg == null) ? 0 : _floatingLeg.hashCode());
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
    FixedFloatSwap other = (FixedFloatSwap) obj;
    if (!ObjectUtils.equals(_fixedLeg, other._fixedLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_floatingLeg, other._floatingLeg)) {
      return false;
    }
    return true;
  }

  public FixedAnnuity getFixedLeg() {
    return _fixedLeg;
  }

  public VariableAnnuity getFloatingLeg() {
    return _floatingLeg;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitSwap(this, curves);
  }

}
