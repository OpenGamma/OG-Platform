/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a fixed coupon bond issue.
 */
public class BondFixedSecurity extends BondSecurity<CouponFixed> {

  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The accrued interest at the settlement date. The accrued interest is and amount (in line with the nominal).
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
   * @param accruedInterest The accrued interest at the settlement date. The accrued interest is and amount (in line with the nominal).
   * @param factorToNextCoupon The factor from spot up to the next coupon.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param couponPerYear Number of coupon per year.
   * @param repoCurveName The name of the curve used for settlement amount discounting.
   */
  public BondFixedSecurity(GenericAnnuity<PaymentFixed> nominal, AnnuityCouponFixed coupon, double settlementTime, double accruedInterest, final double factorToNextCoupon,
      final YieldConvention yieldConvention, int couponPerYear, String repoCurveName) {
    super(nominal, coupon, settlementTime, repoCurveName);
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
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondFixedSecurity(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFixedSecurity(this);
  }

}
