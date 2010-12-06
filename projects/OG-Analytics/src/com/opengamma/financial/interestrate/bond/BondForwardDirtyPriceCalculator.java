/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class BondForwardDirtyPriceCalculator extends BondForwardCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);

  @Override
  public Double calculate(final BondForward bondForward, final YieldCurveBundle curves, final double fundingRate) {
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bondForward.getBond(), curves);
    return calculate(bondForward, dirtyPrice, fundingRate);
  }

  @Override
  public Double calculate(final BondForward bondForward, final double bondDirtyPrice, final double fundingRate) {
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
