/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a fixed coupon bond issue.
 */
public class BondFixedSecurity extends BondSecurity<PaymentFixed, CouponFixed> {

  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The accrued interest at the settlement date. The accrued interest is an amount (in line with the nominal).
   */
  private final double _accruedInterest;
  /**
   * Number of coupon per year.
   */
  private final int _couponPerYear;
  /**
   * The accrual factor to the first coupon. Used for yield computation.
   */
  private final double _factorToNextCoupon;

  /**
   * Fixed coupon bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param settlementTime The time (in years) to settlement date. 
   * @param accruedInterest The accrued interest at the settlement date. The accrued interest is an amount (in line with the nominal).
   * @param factorToNextCoupon The factor from spot up to the next coupon.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param couponPerYear Number of coupon per year.
   * @param repoCurveName The name of the curve used for settlement amount discounting.
   * @param issuer The bond issuer name.
   */
  public BondFixedSecurity(AnnuityPaymentFixed nominal, AnnuityCouponFixed coupon, double settlementTime, double accruedInterest, final double factorToNextCoupon,
      final YieldConvention yieldConvention, int couponPerYear, String repoCurveName, String issuer) {
    super(nominal, coupon, settlementTime, repoCurveName, issuer);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _accruedInterest = accruedInterest;
    _couponPerYear = couponPerYear;
    _factorToNextCoupon = factorToNextCoupon;
  }

  /**
   * Gets the yield computation convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the accrued interest at the settlement date.
   * @return The accrued interest.
   */
  public double getAccruedInterest() {
    return _accruedInterest;
  }

  /**
   * Gets the number of coupon per year.
   * @return The number of coupon per year.
   */
  public int getCouponPerYear() {
    return _couponPerYear;
  }

  /**
   * Gets the accrual factor to the first coupon.
   * @return The accrual factor to the first coupon.
   */
  public double getAccrualFactorToNextCoupon() {
    return _factorToNextCoupon;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondFixedSecurity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFixedSecurity(this);
  }

  @Override
  public String toString() {
    String result = super.toString();
    result += "\nFixed coupon bond: " + _yieldConvention.toString() + ", accrued=" + _accruedInterest + ", coupon=" + _couponPerYear + ", factor=" + _factorToNextCoupon;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _couponPerYear;
    temp = Double.doubleToLongBits(_factorToNextCoupon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _yieldConvention.hashCode();
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
    BondFixedSecurity other = (BondFixedSecurity) obj;
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (Double.doubleToLongBits(_factorToNextCoupon) != Double.doubleToLongBits(other._factorToNextCoupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
