/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public final class BondForwardDirtyPriceCalculator extends BondForwardCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondForwardDirtyPriceCalculator INSTANCE = new BondForwardDirtyPriceCalculator();

  private BondForwardDirtyPriceCalculator() {
  }

  public static BondForwardDirtyPriceCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double calculate(final BondForward bondForward, final YieldCurveBundle curves, final double fundingRate) {
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bondForward.getBond(), curves);
    return calculate(bondForward, dirtyPrice, fundingRate);
  }

  @Override
  public Double calculate(final BondForward bondForward, final double bondDirtyPrice, final double fundingRate) {
    Validate.notNull(bondForward, "bond forward");
    Validate.isTrue(bondDirtyPrice > 0, "bond dirty price is positive");
    Validate.isTrue(fundingRate > 0, "funding rate is positive");
    final Bond bond = bondForward.getBond();
    final double forwardTime = bondForward.getForwardTime();
    final GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();
    double valueOfExpiredCoupons = 0.0;
    for (final FixedCouponPayment payments : coupons.getPayments()) {
      final double ti = payments.getPaymentTime();
      if (ti > forwardTime) {
        break;
      }
      valueOfExpiredCoupons += payments.getAmount() * (1 + fundingRate * (forwardTime - ti));
    }
    return bondDirtyPrice * (1 + fundingRate * forwardTime) - valueOfExpiredCoupons;
  }

}
