/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;

/**
 * Describes a capital inflation indexed bond issue. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon.
 */
public class BondCapitalIndexedSecurity<C extends Coupon> extends BondSecurity<C, C> {

  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param settlementTime The time (in years) to settlement date.
   * @param yieldConvention The bond yield convention.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurity(GenericAnnuity<C> nominal, GenericAnnuity<C> coupon, double settlementTime, YieldConvention yieldConvention, String issuer) {
    super(nominal, coupon, settlementTime, "Not used", issuer);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
  }

  /**
   * Gets the bond yield convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    BondCapitalIndexedSecurity<?> other = (BondCapitalIndexedSecurity<?>) obj;
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
