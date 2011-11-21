/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.market.MarketDiscountingDecorated;
import com.opengamma.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method for inflation bond. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedSecurityDiscountingMethod implements PricingMarketMethod {

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();
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
   * @param market The market.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondCapitalIndexedSecurity<?> bond, final MarketBundle market) {
    Validate.notNull(bond, "Bond");
    MarketBundle creditDiscounting = new MarketDiscountingDecorated(market, bond.getCurrency(), market.getCurve(bond.getIssuer()));
    final CurrencyAmount pvNominal = PVIC.visit(bond.getNominal(), creditDiscounting);
    final CurrencyAmount pvCoupon = PVIC.visit(bond.getCoupon(), creditDiscounting);
    return pvNominal.plus(pvCoupon);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, MarketBundle market) {
    Validate.isTrue(instrument instanceof BondCapitalIndexedSecurity<?>, "Capital inflation indexed bond.");
    return presentValue((BondCapitalIndexedSecurity<?>) instrument, market);
  }

  /**
   * Computes the security present value from a quoted clean real price. The real accrued are added to the clean real price, 
   * the result is multiplied by the inflation index ratio and then discounted from settlement time to 0 with the discounting curve.
   * @param bond The bond security.
   * @param market The market.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedSecurity<Coupon> bond, final MarketBundle market, final double cleanPriceReal) {
    Validate.notNull(bond, "Coupon");
    Validate.notNull(market, "Market");
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    double dirtyPriceReal = cleanPriceReal + bond.getAccruedInterest() / notional;
    double estimatedIndex = bond.getSettlement().estimatedIndex(market);
    double dirtyPriceAjusted = dirtyPriceReal * estimatedIndex / bond.getIndexStartValue();
    double dfSettle = market.getDiscountingFactor(bond.getCurrency(), bond.getSettlementTime());
    double pv = dirtyPriceAjusted * bond.getCoupon().getNthPayment(0).getNotional() * dfSettle;
    return CurrencyAmount.of(bond.getCurrency(), pv);
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
  public double netAmount(final BondCapitalIndexedSecurity<Coupon> bond, final MarketBundle market, final double cleanPriceReal) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    double netAmountReal = cleanPriceReal * notional + bond.getAccruedInterest();
    double estimatedIndex = bond.getSettlement().estimatedIndex(market);
    double netAmount = netAmountReal * estimatedIndex / bond.getIndexStartValue();
    return netAmount;
  }

  /**
   * Computes the dirty real price from the conventional real yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceRealFromYieldReal(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    Validate.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    if (bond.getYieldConvention().equals(SimpleYieldConvention.US_IL_REAL)) {
      // Coupon period rate to next coupon and simple rate from next coupon to settlement.
      double pvAtFirstCoupon;
      if (Math.abs(yield) > 1.0E-8) {
        final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        double vn = Math.pow(factorOnPeriod, 1 - nbCoupon);
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
  public double yieldRealFromDirtyPriceReal(final BondCapitalIndexedSecurity<?> bond, final double dirtyPrice) {
    /**
     * Inner function used to find the yield.
     */
    final Function1D<Double, Double> priceResidual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return dirtyPriceRealFromYieldReal(bond, y) - dirtyPrice;
      }
    };
    final double[] range = BRACKETER.getBracketedPoints(priceResidual, -0.05, 0.10);
    final double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  // TODO: curve sensitivity
  // TODO: price index sensitivity

}
