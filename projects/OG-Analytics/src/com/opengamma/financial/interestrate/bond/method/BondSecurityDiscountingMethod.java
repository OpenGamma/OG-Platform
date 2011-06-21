/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bond security valued by discounting.
 */
public class BondSecurityDiscountingMethod {

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  /**
   * The present value curve sensitivity calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueSensitivityCalculator PVCSC = PresentValueSensitivityCalculator.getInstance();
  /**
   * The root bracket used for yield finding.
   */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /**
   * The root finder used for yield finding.
   */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();

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
   * Computes the present value curve sensitivity of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves) {
    PresentValueSensitivity pvcsNominal = new PresentValueSensitivity(PVCSC.visit(bond.getNominal(), curves));
    PresentValueSensitivity pvcsCoupon = new PresentValueSensitivity(PVCSC.visit(bond.getCoupon(), curves));
    return pvcsNominal.add(pvcsCoupon);
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
   * Computes the dirty price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceFromYield(final BondFixedSecurity bond, final double yield) {
    Validate.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
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
      } 
      // In the last period: simple rate
      return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1.0 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
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

  /**
   * Computes the dirty price sensitivity to the curves.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The price curve sensitivity.
   */
  public PresentValueSensitivity dirtyPriceCurveSensitivity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    double notional = bond.getCoupon().getNthPayment(0).getNotional();
    double pv = presentValue(bond, curves);
    PresentValueSensitivity sensiPv = presentValueCurveSensitivity(bond, curves);
    double df = curves.getCurve(bond.getRepoCurveName()).getDiscountFactor(bond.getSettlementTime());
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listDf = new ArrayList<DoublesPair>();
    listDf.add(new DoublesPair(bond.getSettlementTime(), bond.getSettlementTime() / df));
    resultMap.put(bond.getRepoCurveName(), listDf);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    result = result.multiply(pv / notional);
    result = result.add(sensiPv.multiply(1 / (df * notional)));
    return result;
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
   * Computes the clean price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The clean price.
   */
  public double cleanPriceFromYield(final BondFixedSecurity bond, final double yield) {
    double dirtyPrice = dirtyPriceFromYield(bond, yield);
    double cleanPrice = cleanPriceFromDirtyPrice(bond, dirtyPrice);
    return cleanPrice;
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The yield.
   */
  public double yieldFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    /**
     * Inner function used to find the yield.
     */
    final Function1D<Double, Double> priceResidual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return dirtyPriceFromYield(bond, y) - dirtyPrice;
      }
    };
    final double[] range = BRACKETER.getBracketedPoints(priceResidual, 0.00, 0.20);
    double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The yield.
   */
  public double yieldFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    double dirtyPrice = dirtyPriceFromCurves(bond, curves);
    double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Compute the conventional yield from the clean price.
   * @param bond The bond security.
   * @param cleanPrice The bond clean price.
   * @return The yield.
   */
  public double yieldFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    double dirtyPrice = dirtyPriceFromCleanPrice(bond, cleanPrice);
    double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Computes the modified duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The modified duration.
   */
  public double modifiedDurationFromYield(final BondFixedSecurity bond, final double yield) {
    int nbCoupon = bond.getCoupon().getNumberOfPayments();
    double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) {
      if (nbCoupon > 1) { // More than one coupon left
        double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        double mdAtFirstCoupon = 0;
        double pvAtFirstCoupon = 0;
        for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
          mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
          pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
        }
        mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
        pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
        double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
        double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
        return md;
      } 
      // In the last period: simple rate
      return bond.getAccrualFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear());
    } else if (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) {
      double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double mdAtFirstCoupon = 0;
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
      double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
      return md;
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getConventionName() + " is not supported.");
  }

  /**
   * Computes the modified duration of a bond from the curves.
   * @param bond  The bond security.
   * @param curves The curve bundle.
   * @return The modified duration.
   */
  public double modifiedDurationFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    double yield = yieldFromCurves(bond, curves);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the modified duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The modified duration.
   */
  public double modifiedDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return modifiedDurationFromYield(bond, yield);
  }

}
