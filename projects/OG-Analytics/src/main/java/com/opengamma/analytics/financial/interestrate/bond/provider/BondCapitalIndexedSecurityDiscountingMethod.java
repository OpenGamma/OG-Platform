/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Pricing method for inflation bond. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedSecurityDiscountingMethod {

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  //TODO: REVIEW: Method depends on Calculator; Calculator would depend on Method (code duplicated to avoid circularity).
  /**
   * The root bracket used for yield finding.
   */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /**
   * The root finder used for yield finding.
   */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();

  /**
   * Computes the present value of a capital indexed bound by index estimation and discounting. The value is the value of the nominal and the coupons but not the settlement.
   * @param bond The bond.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondCapitalIndexedSecurity<?> bond, final InflationIssuerProviderInterface provider) {
    ArgumentChecker.notNull(bond, "Bond");
    final InflationProviderInterface creditDiscounting = provider.withDiscountFactor(bond.getCurrency(), new ObjectsPair<>(bond.getIssuer(), bond.getCurrency()));
    final MultipleCurrencyAmount pvNominal = bond.getNominal().accept(PVIC, creditDiscounting);
    final MultipleCurrencyAmount pvCoupon = bond.getCoupon().accept(PVIC, creditDiscounting);
    return pvNominal.plus(pvCoupon);
  }

  /**
   * Computes the security present value from a quoted clean real price. The real accrued are added to the clean real price,
   * the result is multiplied by the inflation index ratio and then discounted from settlement time to 0 with the discounting curve.
   * @param bond The bond security.
   * @param market The market.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedSecurity<Coupon> bond, final InflationIssuerProviderInterface market, final double cleanPriceReal) {
    Validate.notNull(bond, "Coupon");
    Validate.notNull(market, "Market");
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final double dirtyPriceReal = cleanPriceReal + bond.getAccruedInterest() / notional;
    final MultipleCurrencyAmount pv = bond.getSettlement().accept(PVIC, market.getInflationProvider());
    return pv.multipliedBy(dirtyPriceReal);
  }

  /**
   * Computes the clean real price of a bond security from a dirty real price.
   * @param bond The bond security.
   * @param dirtyPrice The dirty price.
   * @return The clean price.
   */
  public double cleanRealPriceFromDirtyRealPrice(final BondCapitalIndexedSecurity<?> bond, final double dirtyPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return dirtyPrice - bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the dirty real price of a bond security from the clean real price.
   * @param bond The bond security.
   * @param cleanPrice The clean price.
   * @return The clean price.
   */
  public double dirtyRealPriceFromCleanRealPrice(final BondCapitalIndexedSecurity<?> bond, final double cleanPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return cleanPrice + bond.getAccruedInterest() / notional;
  }

  /**
   * The net amount paid at settlement date for a given clean real price.
   * @param bond The bond.
   * @param market The market.
   * @param cleanPriceReal The clean real price.
   * @return The net amount.
   */
  public MultipleCurrencyAmount netAmount(final BondCapitalIndexedSecurity<Coupon> bond, final InflationIssuerProviderInterface market, final double cleanPriceReal) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final double netAmountRealByUnit = cleanPriceReal + bond.getAccruedInterest() / notional;
    final MultipleCurrencyAmount netAmount = bond.getSettlement().accept(NAIC, market.getInflationProvider());
    return netAmount.multipliedBy(netAmountRealByUnit);

  }

  /**
   * Computes the dirty real price from the conventional real yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyRealPriceFromYieldReal(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    Validate.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_IL_REAL)) {
      // Coupon period rate to next coupon and simple rate from next coupon to settlement.
      double pvAtFirstCoupon;
      if (Math.abs(yield) > 1.0E-8) {
        final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        final double vn = Math.pow(factorOnPeriod, 1 - nbCoupon);
        pvAtFirstCoupon = ((CouponInflationGearing) bond.getCoupon().getNthPayment(0)).getFactor() / yield * (factorOnPeriod - vn) + vn;
      } else {
        pvAtFirstCoupon = ((CouponInflationGearing) bond.getCoupon().getNthPayment(0)).getFactor() / bond.getCouponPerYear() * nbCoupon + 1;
      }
      return pvAtFirstCoupon / (1 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear());
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getConventionName() + " is not supported.");
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The yield.
   */
  public double yieldRealFromDirtyRealPrice(final BondCapitalIndexedSecurity<?> bond, final double dirtyPrice) {
    /**
     * Inner function used to find the yield.
     */
    final Function1D<Double, Double> priceResidual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return dirtyRealPriceFromYieldReal(bond, y) - dirtyPrice;
      }
    };
    final double[] range = BRACKETER.getBracketedPoints(priceResidual, -0.05, 0.10);
    final double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  /**
   * Computes the present value sensitivity of a capital indexed bound by index estimation and discounting. The sensitivity is the sensitivity of the nominal and the coupons but not the settlement.
   * @param bond The bond.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final BondCapitalIndexedSecurity<?> bond, final InflationIssuerProviderInterface provider) {
    ArgumentChecker.notNull(bond, "Bond");
    final InflationProviderInterface creditDiscounting = provider.withDiscountFactor(bond.getCurrency(), new ObjectsPair<>(bond.getIssuer(), bond.getCurrency()));
    final MultipleCurrencyInflationSensitivity sensitivityNominal = bond.getNominal().accept(PVCSIC, creditDiscounting);
    final MultipleCurrencyInflationSensitivity sensitivityCoupon = bond.getCoupon().accept(PVCSIC, creditDiscounting);
    return sensitivityNominal.plus(sensitivityCoupon);
  }

}
