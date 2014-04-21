/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.singlevalue;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesMarginIndexFromPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesMarginIndexFromPriceCalculator INSTANCE = new FuturesMarginIndexFromPriceCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesMarginIndexFromPriceCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesMarginIndexFromPriceCalculator() {
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final Double quotedPrice) {
    return quotedPrice * futures.getNotional() * futures.getPaymentAccrualFactor();
  }

  @Override
  public Double visitBondFuturesSecurity(final BondFuturesSecurity futures, final Double quotedPrice) {
    return quotedPrice * futures.getNotional();
  }

  @Override
  public Double visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity futures, final Double quotedPrice) {
    return quotedPrice * futures.getNotional() * futures.getPaymentAccrualFactor();
  }

  @Override
  public Double visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final Double quotedPrice) {
    return quotedPrice * futures.getNotional();
  }

  @Override
  public Double visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity futures, final Double quotedPrice) {
    final double yield = 1.0d - quotedPrice;
    final double dirtyPrice = dirtyPriceFromYield(yield, futures.getCouponRate(), futures.getTenor(), futures.getNumberCouponPerYear());
    return dirtyPrice * futures.getNotional();
  }

  /**
   * The dirty price from the standard yield.
   * @param yield The yield
   * @param coupon The coupon
   * @param tenor The tenor (in year)
   * @param couponPerYear Number of coupon per year.
   * @return The price.
   */
  private double dirtyPriceFromYield(final double yield, final double coupon, final int tenor, final int couponPerYear) {
    final double v = 1.0d + yield / couponPerYear;
    final int n = tenor * couponPerYear;
    final double vn = Math.pow(v, -n);
    return coupon / yield * (1 - vn) + vn;
  }

  //-----     Futures options     -----

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, Double quotedPrice) {
    return quotedPrice * option.getUnderlyingFuture().getNotional() * option.getUnderlyingFuture().getPaymentAccrualFactor();
  }

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option, Double quotedPrice) {
    return quotedPrice * option.getUnderlyingFuture().getNotional();
  }

}
