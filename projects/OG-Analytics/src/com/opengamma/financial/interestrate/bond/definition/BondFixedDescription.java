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
public class BondFixedDescription extends BondDescription<CouponFixed> {
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;

  /**
   * Fixed coupon bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   */
  public BondFixedDescription(GenericAnnuity<PaymentFixed> nominal, AnnuityCouponFixed coupon, YieldConvention yieldConvention) {
    super(nominal, coupon);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
  }

  /**
   * Gets the yield computation convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return null;
  }

}
