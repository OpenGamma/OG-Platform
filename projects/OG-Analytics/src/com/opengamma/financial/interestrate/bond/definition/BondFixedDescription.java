/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a fixed coupon bond issue.
 */
public class BondFixedDescription extends BondDescription<CouponFixed> {

  /**
   * Accrued interest at standard settlement date (spot).
   */
  private final double _accruedInterestAtSpot;

  /**
   * Fixed coupon bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param settlementTime Time to standard settlement.
   * @param accruedInterestAtSpot Accrued interest at standard settlement date (spot).
   */
  public BondFixedDescription(GenericAnnuity<PaymentFixed> nominal, AnnuityCouponFixed coupon, double settlementTime, double accruedInterestAtSpot) {
    super(nominal, coupon, settlementTime);
    // TODO: Check coupon and nominal rate wrt spotTime.
    _accruedInterestAtSpot = accruedInterestAtSpot;
  }

  /**
   * Gets the accrued interest at standard settlement date (spot).
   * @return The accrued interest.
   */
  public double getAccruedInterestAtSpot() {
    return _accruedInterestAtSpot;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return null;
  }

  @Override
  public String toString() {
    String result = super.toString();
    result += "\n" + "Accrued interest at spot: " + _accruedInterestAtSpot;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_accruedInterestAtSpot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFixedDescription other = (BondFixedDescription) obj;
    if (Double.doubleToLongBits(_accruedInterestAtSpot) != Double.doubleToLongBits(other._accruedInterestAtSpot)) {
      return false;
    }
    return true;
  }

}
