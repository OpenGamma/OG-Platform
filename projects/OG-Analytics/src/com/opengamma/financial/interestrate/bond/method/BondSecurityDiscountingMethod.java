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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bond security valued by discounting.
 */
public final class BondSecurityDiscountingMethod {

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
  private static final BracketRoot ROOT_BRACKETER = new BracketRoot();
  /**
   * The unique instance of the class.
   */
  private static final BondSecurityDiscountingMethod INSTANCE = new BondSecurityDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondSecurityDiscountingMethod() {
  }

  /**
   * Computes the present value of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves) {
    final double pvNominal = PVC.visit(bond.getNominal(), curves);
    final double pvCoupon = PVC.visit(bond.getCoupon(), curves);
    return pvNominal + pvCoupon;
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public double presentValueFromCleanPrice(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves, final double cleanPrice) {
    Validate.isTrue(bond instanceof BondFixedSecurity, "Present value from clean price available only for fixed coupon bond");
    final BondFixedSecurity bondFixed = (BondFixedSecurity) bond;
    final double dfSettle = curves.getCurve(bondFixed.getRepoCurveName()).getDiscountFactor(bondFixed.getSettlementTime());
    final double pvPrice = (cleanPrice * bondFixed.getCoupon().getNthPayment(0).getNotional() + bondFixed.getAccruedInterest()) * dfSettle;
    return pvPrice;
  }

  /**
   * Computes the present value of a bond security from z-spread. The z-spread is a parallel shift applied to the discounting curve associated to the bond.
   * The parallel shift is done in the curve convention.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @param zSpread The z-spread.
   * @return The present value.
   */
  public double presentValueFromZSpread(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves, final double zSpread) {
    String discountingCurveName = bond.getDiscountingCurveName();
    YieldCurveBundle curvesWithZ = new YieldCurveBundle();
    curvesWithZ.addAll(curves);
    YieldAndDiscountCurve shiftedDiscounting = curves.getCurve(discountingCurveName).withParallelShift(zSpread);
    curvesWithZ.replaceCurve(discountingCurveName, shiftedDiscounting);
    double result = presentValue(bond, curvesWithZ);
    return result;
  }

  /**
   * Computes the present value curve sensitivity of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves) {
    final PresentValueSensitivity pvcsNominal = new PresentValueSensitivity(PVCSC.visit(bond.getNominal(), curves));
    final PresentValueSensitivity pvcsCoupon = new PresentValueSensitivity(PVCSC.visit(bond.getCoupon(), curves));
    return pvcsNominal.add(pvcsCoupon);
  }

  /**
   * Compute the dirty price of a bond security from curves.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The dirty price.
   */
  public double dirtyPriceFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double pv = presentValue(bond, curves);
    final double df = curves.getCurve(bond.getRepoCurveName()).getDiscountFactor(bond.getSettlementTime());
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return pv / df / notional;
  }

  /**
   * Computes the dirty price of a bond security from a clean price.
   * @param bond The bond security.
   * @param cleanPrice The clean price.
   * @return The dirty price.
   */
  public double dirtyPriceFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
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
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) {
      if (nbCoupon > 1) { // More than one coupon left
        final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        double pvAtFirstCoupon = 0;
        for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
          pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
        }
        pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
        return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / nominal;
      } // In the last period: simple rate
      return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1.0 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
    } else if (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) {
      final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
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
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final double pv = presentValue(bond, curves);
    final PresentValueSensitivity sensiPv = presentValueCurveSensitivity(bond, curves);
    final double df = curves.getCurve(bond.getRepoCurveName()).getDiscountFactor(bond.getSettlementTime());
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDf = new ArrayList<DoublesPair>();
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
    final double dirtyPrice = dirtyPriceFromCurves(bond, curves);
    return cleanPriceFromDirtyPrice(bond, dirtyPrice);
  }

  /**
   * Computes the clean price of a bond security from a dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The dirty price.
   * @return The clean price.
   */
  public double cleanPriceFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return dirtyPrice - bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the clean price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The clean price.
   */
  public double cleanPriceFromYield(final BondFixedSecurity bond, final double yield) {
    final double dirtyPrice = dirtyPriceFromYield(bond, yield);
    final double cleanPrice = cleanPriceFromDirtyPrice(bond, dirtyPrice);
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
    final double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The yield.
   */
  public double yieldFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double dirtyPrice = dirtyPriceFromCurves(bond, curves);
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Compute the conventional yield from the clean price.
   * @param bond The bond security.
   * @param cleanPrice The bond clean price.
   * @return The yield.
   */
  public double yieldFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double dirtyPrice = dirtyPriceFromCleanPrice(bond, cleanPrice);
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Computes the modified duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The modified duration.
   */
  public double modifiedDurationFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) && (nbCoupon == 1)) {
      return bond.getAccrualFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear());
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD))) {
      final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double mdAtFirstCoupon = 0;
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
      final double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
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
    final double yield = yieldFromCurves(bond, curves);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the modified duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The modified duration.
   */
  public double modifiedDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macauley duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The Macauley duration.
   */
  public double macauleyDurationFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) {
      if (nbCoupon > 1) { // More than one coupon left
        return modifiedDurationFromYield(bond, yield) * (1 + yield / bond.getCouponPerYear());
      } else {
        return bond.getAccrualFactorToNextCoupon() / bond.getCouponPerYear();
      }
    } else if (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) {
      return modifiedDurationFromYield(bond, yield) * (1 + yield / bond.getCouponPerYear());
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getConventionName() + " is not supported.");
  }

  /**
   * Computes the Macauley duration of a bond from the curves.
   * @param bond  The bond security.
   * @param curves The curve bundle.
   * @return The Macauley duration.
   */
  public double macauleyDurationFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double yield = yieldFromCurves(bond, curves);
    return macauleyDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macauley duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The Macauley duration.
   */
  public double macauleyDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return macauleyDurationFromYield(bond, yield);
  }

  /**
   * Computes the convexity of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The convexity.
   */
  public double convexityFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) && (nbCoupon == 1)) {
      double timeToPay = bond.getAccrualFactorToNextCoupon() / bond.getCouponPerYear();
      double disc = (1.0 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear());
      return 2 * timeToPay * timeToPay / (disc * disc);
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD))) {
      final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double cvAtFirstCoupon = 0;
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        cvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 2) * (loopcpn + bond.getAccrualFactorToNextCoupon())
            * (loopcpn + bond.getAccrualFactorToNextCoupon() + 1) / (bond.getCouponPerYear() * bond.getCouponPerYear());
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      cvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon + 1) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) * (nbCoupon + bond.getAccrualFactorToNextCoupon())
          / (bond.getCouponPerYear() * bond.getCouponPerYear());
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
      final double cv = cvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
      return cv;
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getConventionName() + " is not supported.");
  }

  /**
   * Computes the convexity of a bond from the curves.
   * @param bond  The bond security.
   * @param curves The curve bundle.
   * @return The convexity.
   */
  public double convexityFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double yield = yieldFromCurves(bond, curves);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes the convexity of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The convexity.
   */
  public double convexityFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param pv The target present value.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndPV(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves, final double pv) {
    Validate.notNull(bond, "bond");
    Validate.notNull(curves, "curves");

    final Function1D<Double, Double> residual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return presentValueFromZSpread(bond, curves, z) - pv;
      }
    };

    final double[] range = ROOT_BRACKETER.getBracketedPoints(residual, -0.01, 0.01); // Starting range is [-1%, 1%]
    return ROOT_FINDER.getRoot(residual, range[0], range[1]);
  }

  /**
   * Computes a bond z-spread from the curves and a clean price.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param cleanPrice The target clean price.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndClean(final BondSecurity<? extends Payment> bond, final YieldCurveBundle curves, final double cleanPrice) {
    return zSpreadFromCurvesAndPV(bond, curves, presentValueFromCleanPrice(bond, curves, cleanPrice));
  }

}
