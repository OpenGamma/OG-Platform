/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueParallelCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bond security valued by discounting.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod}
 */
@Deprecated
public final class BondSecurityDiscountingMethod {
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
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  /**
   * The present value curve sensitivity calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  /**
   * The present value parallel curve sensitivity calculator
   */
  private static final PresentValueParallelCurveSensitivityCalculator PVPCSC = PresentValueParallelCurveSensitivityCalculator.getInstance();
  /**
   * The root bracket used for yield finding.
   */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /**
   * The root finder used for yield finding.
   */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();
  /**
   * Brackets a root
   */
  private static final BracketRoot ROOT_BRACKETER = new BracketRoot();

  /**
   * Computes the present value of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves) {
    final double pvNominal = bond.getNominal().accept(PVC, curves);
    final double pvCoupon = bond.getCoupon().accept(PVC, curves);
    return pvNominal + pvCoupon;
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public double presentValueFromCleanPrice(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double cleanPrice) {
    ArgumentChecker.isTrue(bond instanceof BondFixedSecurity, "Present value from clean price available only for fixed coupon bond");
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
  public double presentValueFromZSpread(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double zSpread) {
    final String discountingCurveName = bond.getDiscountingCurveName();
    final YieldCurveBundle curvesWithZ = new YieldCurveBundle();
    curvesWithZ.addAll(curves);
    final YieldAndDiscountCurve shiftedDiscounting = curves.getCurve(discountingCurveName).withParallelShift(zSpread);
    curvesWithZ.replaceCurve(discountingCurveName, shiftedDiscounting);
    final double result = presentValue(bond, curvesWithZ);
    return result;
  }

  /**
   * Calculates the sensitivity of the bond to the z spread
   * @param bond The bond
   * @param curves The curves
   * @param zSpread The z spread
   * @return The sensitivity
   */
  public double presentValueZSpreadSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double zSpread) {
    final String discountingCurveName = bond.getDiscountingCurveName();
    final YieldCurveBundle curvesWithZ = new YieldCurveBundle();
    curvesWithZ.addAll(curves);
    final YieldAndDiscountCurve shiftedDiscounting = curves.getCurve(discountingCurveName).withParallelShift(zSpread);
    curvesWithZ.replaceCurve(discountingCurveName, shiftedDiscounting);
    final StringAmount parallelSensi = presentValueParallelCurveSensitivity(bond, curvesWithZ);
    return parallelSensi.getMap().get(discountingCurveName);

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
   * @param bond  The bond security, not null
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceFromYield(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    if (((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.GERMAN_BOND)))
        && (nbCoupon == 1)) {
      return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD)) && (nbCoupon == 1)) {
      return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / nominal * Math.pow(1.0 + yield / bond.getCouponPerYear(), -bond.getFactorToNextCoupon());
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.GERMAN_BOND)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD))) {
      return dirtyPriceFromYieldStandard(bond, yield);
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + " is not supported.");
  }

  /**
   * Calculates the dirty price from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The dirty price
   */
  private double dirtyPriceFromYieldStandard(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(0).getAmount();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
    }
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / nominal;
  }

  /**
   * Computes the dirty price sensitivity to the curves.
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The price curve sensitivity.
   */
  public InterestRateCurveSensitivity dirtyPriceCurveSensitivity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final double pv = presentValue(bond, curves);
    final InterestRateCurveSensitivity sensiPv = presentValueCurveSensitivity(bond, curves);
    final double df = curves.getCurve(bond.getRepoCurveName()).getDiscountFactor(bond.getSettlementTime());
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listDf = new ArrayList<>();
    listDf.add(DoublesPair.of(bond.getSettlementTime(), bond.getSettlementTime() / df));
    resultMap.put(bond.getRepoCurveName(), listDf);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    result = result.multipliedBy(pv / notional);
    result = result.plus(sensiPv.multipliedBy(1 / (df * notional)));
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
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(curves, "Curves");
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
      return bond.getFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear());
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD)) && (nbCoupon == 1)) {
      return bond.getFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + yield / bond.getCouponPerYear());
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.GERMAN_BOND)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD))) {
      final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double mdAtFirstCoupon = 0;
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getFactorToNextCoupon()) / bond.getCouponPerYear();
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getFactorToNextCoupon()) / bond.getCouponPerYear();
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon());
      final double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / pv;
      return md;
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + " is not supported.");
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
   * Computes the modified duration of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The modified duration.
   */
  public double modifiedDurationFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double yield = yieldFromCleanPrice(bond, cleanPrice);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macaulay duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The Macaulay duration.
   */
  public double macaulayDurationFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    if (((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD))) && (nbCoupon == 1)) {
      return bond.getFactorToNextCoupon() / bond.getCouponPerYear();
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.GERMAN_BOND)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD))) {
      return modifiedDurationFromYield(bond, yield) * (1 + yield / bond.getCouponPerYear());
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + " is not supported for Macaulay duration.");
  }

  /**
   * Computes the Macaulay duration of a bond from the curves.
   * @param bond  The bond security.
   * @param curves The curve bundle.
   * @return The Macaulay duration.
   */
  public double macaulayDurationFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final double yield = yieldFromCurves(bond, curves);
    return macaulayDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macauley duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The Macauley duration.
   */
  public double macaulayDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return macaulayDurationFromYield(bond, yield);
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
    final YieldConvention yieldConvention = bond.getYieldConvention();
    if (nbCoupon == 1) {
      if (((yieldConvention.equals(SimpleYieldConvention.US_STREET)) || (yieldConvention.equals(SimpleYieldConvention.GERMAN_BOND)))) {
        final double timeToPay = bond.getFactorToNextCoupon() / bond.getCouponPerYear();
        final double disc = (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear());
        return 2 * timeToPay * timeToPay / (disc * disc);
      }
      if (yieldConvention.equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD)) {
        throw new UnsupportedOperationException("The convention " + yieldConvention.getName() + "with only one coupon is not supported.");
      }
    }
    if ((yieldConvention.equals(SimpleYieldConvention.US_STREET)) || (yieldConvention.equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) ||
        (yieldConvention.equals(SimpleYieldConvention.GERMAN_BOND)) || (yieldConvention.equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD))) {
      final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
      double cvAtFirstCoupon = 0;
      double pvAtFirstCoupon = 0;
      for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
        cvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 2) * (loopcpn + bond.getFactorToNextCoupon())
            * (loopcpn + bond.getFactorToNextCoupon() + 1) / (bond.getCouponPerYear() * bond.getCouponPerYear());
        pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
      }
      cvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon + 1) * (nbCoupon - 1 + bond.getFactorToNextCoupon()) * (nbCoupon + bond.getFactorToNextCoupon())
          / (bond.getCouponPerYear() * bond.getCouponPerYear());
      pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
      final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon());
      final double cv = cvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / pv;
      return cv;
    }
    throw new UnsupportedOperationException("The convention " + yieldConvention.getName() + " is not supported.");
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
   * Computes the convexity of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The convexity.
   */
  public double convexityFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double yield = yieldFromCleanPrice(bond, cleanPrice);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param pv The target present value.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndPV(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double pv) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(curves, "curves");

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
   * Computes a bond present value z-spread sensitivity from the curves and a present value.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param pv The target present value.
   * @return The z-spread sensitivity.
   */
  public double presentValueZSpreadSensitivityFromCurvesAndPV(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double pv) {
    final double zSpread = zSpreadFromCurvesAndPV(bond, curves, pv);
    return presentValueZSpreadSensitivity(bond, curves, zSpread);
  }

  /**
   * Computes a bond z-spread from the curves and a clean price.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param cleanPrice The target clean price.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndClean(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double cleanPrice) {
    return zSpreadFromCurvesAndPV(bond, curves, presentValueFromCleanPrice(bond, curves, cleanPrice));
  }

  /**
   * Computes the bond present value z-spread sensitivity from the curves and a clean price.
   * @param bond The bond.
   * @param curves The curve bundle.
   * @param cleanPrice The target clean price.
   * @return The z-spread sensitivity.
   */
  public double presentValueZSpreadSensitivityFromCurvesAndClean(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves, final double cleanPrice) {
    return presentValueZSpreadSensitivityFromCurvesAndPV(bond, curves, presentValueFromCleanPrice(bond, curves, cleanPrice));
  }

  /**
   * Computes the present value curve sensitivity of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves) {
    final InterestRateCurveSensitivity pvcsNominal = new InterestRateCurveSensitivity(bond.getNominal().accept(PVCSC, curves));
    final InterestRateCurveSensitivity pvcsCoupon = new InterestRateCurveSensitivity(bond.getCoupon().accept(PVCSC, curves));
    return pvcsNominal.plus(pvcsCoupon);
  }

  /**
   * Computes the present value curve sensitivity to parallel curve movement of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param curves The curve bundle.
   * @return The present value curve sensitivity.
   */
  public StringAmount presentValueParallelCurveSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final YieldCurveBundle curves) {
    final StringAmount pvpcsNominal = bond.getNominal().accept(PVPCSC, curves);
    final StringAmount pvpcsCoupon = bond.getCoupon().accept(PVPCSC, curves);
    return StringAmount.plus(pvpcsNominal, pvpcsCoupon);
  }

  /**
   * Calculates the accrued interest for a fixed-coupon bond using the curves. The accrued interest is defined
   * as dirty price - clean price.
   * @param bond The bond, not null
   * @param curves The curves, not null
   * @return The accrued interest
   */
  public double accruedInterestFromCurves(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(curves, "curves");
    return dirtyPriceFromCurves(bond, curves) - cleanPriceFromCurves(bond, curves);
  }
}
