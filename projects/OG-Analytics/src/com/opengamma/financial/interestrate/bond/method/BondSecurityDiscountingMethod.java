/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Class with methods related to bond security valued by discounting.
 */
public class BondSecurityDiscountingMethod {

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Compute the present value of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves) {
    double pvNominal = PVC.visit(bond.getNominal(), curves);
    double pvCoupon = PVC.visit(bond.getCoupon(), curves);
    return pvNominal + pvCoupon;
  }

  /**
   * Compute the dirty price of a bond security from curves.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The dirty price.
   */
  public double dirtyPriceFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    double pv = presentValue(bond, curves);
    double df = curves.getCurve(bond.getRepoCurveName()).getDiscountFactor(bond.getSettlementTime());
    double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return pv / df / notional;
  }

  /**
   * Computes the clean price of a bond security from curves.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The clean price.
   */
  public double cleanPriceFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    double dirtyPrice = dirtyPriceFromCurves(bond, curves);
    return cleanPriceFromDirtyPrice(bond, dirtyPrice);
  }

  /**
   * Computes the clean price of a bond security from a dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The dirty price.
   * @return The clean price.
   */
  public double cleanPriceFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return dirtyPrice - bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the dirty price of a bond security from a clean price.
   * @param bond The bond security.
   * @param cleanPrice The clean price.
   * @return The dirty price.
   */
  public double dirtyPriceFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return cleanPrice + bond.getAccruedInterest() / notional;
  }

  /**
   * Compute the dirty price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceFromYield(final BondFixedSecurity bond, final double yield) {
    int nbCoupon = bond.getCoupon().getNumberOfPayments();
    double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) {
      if (nbCoupon > 1) { // More than one coupon left
        double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        double pvAtFirstCoupon = 0;
        for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
          pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
        }
        pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
        return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / nominal;
      } else { // In the last period: simple rate
        return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
      }
    } else if (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) {
      double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / nominal;
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getConventionName() + " is not supported.");
  }

}
