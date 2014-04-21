/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BondTotalReturnSwap extends TotalReturnSwap {
  /** The bond */
  private final BondSecurity<? extends Payment, ? extends Coupon> _bond;

  /**
   * @param fundingLeg The funding leg, not null
   * @param bond The bond, not null
   */
  public BondTotalReturnSwap(final Annuity<? extends Payment> fundingLeg, final BondSecurity<? extends Payment, ? extends Coupon> bond) {
    super(fundingLeg);
    ArgumentChecker.notNull(bond, "bond");
    _bond = bond;
  }

  /**
   * Gets the bond bond.
   * @return The bond
   */
  public BondSecurity<? extends Payment, ? extends Coupon> getAsset() {
    return _bond;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondTotalReturnSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondTotalReturnSwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _bond.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BondTotalReturnSwap)) {
      return false;
    }
    final BondTotalReturnSwap other = (BondTotalReturnSwap) obj;
    if (!ObjectUtils.equals(_bond, other._bond)) {
      return false;
    }
    return true;
  }


}
