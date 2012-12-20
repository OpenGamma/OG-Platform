/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketForwardSensitivity;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with gearing factor and spread.
 */
public final class CouponIborDiscountingMarketMethod implements PricingMarketMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborDiscountingMarketMethod INSTANCE = new CouponIborDiscountingMarketMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborDiscountingMarketMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborDiscountingMarketMethod() {
  }

  /**
   * Compute the present value of a Ibor coupon by discounting.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIbor coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Market");
    final double forward = market.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final IMarketBundle market) {
    throw new NotImplementedException("Cannot handle CouponIborGearing");
    //FIXME As this code stands, it goes into an infinite loop
    //    ArgumentChecker.isTrue(instrument instanceof CouponIborGearing, "Coupon Ibor Gearing");
    //    return presentValue((CouponIborGearing) instrument, market);
  }

  /**
   * Compute the present value sensitivity to yield for discounting curve and forward rate (in index convention) for forward curve.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueMarketSensitivity(final CouponIbor coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Curves");
    final double forward = market.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df * pvBar;
    final double dfBar = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(market.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<MarketForwardSensitivity>> mapFwd = new HashMap<String, List<MarketForwardSensitivity>>();
    final List<MarketForwardSensitivity> listForward = new ArrayList<MarketForwardSensitivity>();
    listForward.add(new MarketForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(market.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyCurveSensitivityMarket result = MultipleCurrencyCurveSensitivityMarket.of(coupon.getCurrency(),
        CurveSensitivityMarket.ofYieldDiscountingAndForward(mapDsc, mapFwd));
    return result;
  }

  //  /**
  //   * TODO: is this method required?
  //   * Compute the present value sensitivity to rates from today (continuously compounded) for the discounting and forward curve.
  //   * @param coupon The coupon.
  //   * @param market The market curves.
  //   * @return The present value sensitivity.
  //   */
  //  public CurveSensitivityMarket presentValueYieldSensitivity(final CouponIbor coupon, final MarketDiscountBundle market) {
  //    ArgumentChecker.notNull(coupon, "Coupon");
  //    ArgumentChecker.notNull(market, "Curves");
  //    final YieldAndDiscountCurve forwardCurve = market.getCurve(coupon.getIndex());
  //    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
  //    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
  //    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
  //    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / coupon.getFixingAccrualFactor();
  //    // Backward sweep
  //    final double pvBar = 1.0;
  //    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df * pvBar;
  //    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingAccrualFactor() * forwardBar;
  //    final double dfForwardStartBar = 1.0 / (coupon.getFixingAccrualFactor() * dfForwardEnd) * forwardBar;
  //    final double dfBar = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * pvBar;
  //    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
  //    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
  //    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
  //    mapDsc.put(market.getCurve(coupon.getCurrency()).getName(), listDiscounting);
  //    final Map<String, List<DoublesPair>> mapFwd = new HashMap<String, List<DoublesPair>>();
  //    final List<DoublesPair> listForward = new ArrayList<DoublesPair>();
  //    listForward.add(new DoublesPair(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
  //    listForward.add(new DoublesPair(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
  //    mapFwd.put(market.getCurve(coupon.getIndex()).getName(), listForward);
  //    final CurveSensitivityMarket result = CurveSensitivityMarket.fromYieldDiscountingAndForward(mapDsc, mapFwd);
  //    return result;
  //  }

}
