/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.derivative.CouponIborRatchet;

/**
 * A wrapper class for a AnnuityDefinition containing mainly CouponIborRatchetDefinition. The first coupon should be a CouponFixed or a CouponIborGearing.
 * The other coupons should be CouponFixed or a CouponIborRatchet.
 */
public class AnnuityCouponIborRatchet extends GenericAnnuity<Coupon> {

  /**
   * Flag indicating if a coupon is already fixed.
   */
  private final boolean[] _isFixed;

  /**
   * @param payments The payments composing the annuity.
   */
  public AnnuityCouponIborRatchet(Coupon[] payments) {
    super(payments);
    _isFixed = new boolean[payments.length];
    Validate.isTrue((payments[0] instanceof CouponFixed) || (payments[0] instanceof CouponIborGearing), "First coupon should be CouponFixed or a CouponIborGearing");
    _isFixed[0] = (payments[0] instanceof CouponFixed);
    for (int looppay = 1; looppay < payments.length; looppay++) {
      Validate.isTrue((payments[looppay] instanceof CouponFixed) || (payments[looppay] instanceof CouponIborRatchet), "Next coupons should be CouponFixed or CouponIborRatchet");
      _isFixed[looppay] = (payments[looppay] instanceof CouponFixed);
    }
  }

  /**
   * Gets the flag indicating if a coupon is already fixed.
   * @return The flag.
   */
  public boolean[] isFixed() {
    return _isFixed;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitAnnuityCouponIborRatchet(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitAnnuityCouponIborRatchet(this);
  }

}
