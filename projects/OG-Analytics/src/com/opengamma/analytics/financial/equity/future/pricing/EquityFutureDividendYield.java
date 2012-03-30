/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.future.EquityFutureDataBundle;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;

/**
 * Method to compute a future's present value given the current value of its underlying,
 * an estimate of its deterministic continuous dividend yield, and a funding curve   
 */
public final class EquityFutureDividendYield implements EquityFuturesPricer {

  private static final EquityFutureDividendYield INSTANCE = new EquityFutureDividendYield();

  /**
   * @return singleton instance of this pricing method
   */
  public static EquityFutureDividendYield getInstance() {
    return INSTANCE;
  }

  private EquityFutureDividendYield() {
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return Present value of the derivative
   */
  @Override
  public double presentValue(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getFundingCurve());
    Validate.notNull(dataBundle.getSpotValue());
    Validate.notNull(dataBundle.getDividendYield());

    double timeToExpiry = future.getTimeToSettlement();
    double discountRate = dataBundle.getFundingCurve().getInterestRate(timeToExpiry);
    double costOfCarry = Math.exp(timeToExpiry * (discountRate - dataBundle.getDividendYield()));
    double fwdPrice = dataBundle.getSpotValue() * costOfCarry;
    return (fwdPrice - future.getStrike()) * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the underlying's spot value
   */
  @Override
  public double spotDelta(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getFundingCurve());
    Validate.notNull(dataBundle.getDividendYield());

    double timeToExpiry = future.getTimeToSettlement();
    double discountRate = dataBundle.getFundingCurve().getInterestRate(timeToExpiry);
    double costOfCarry = Math.exp(timeToExpiry * (discountRate - dataBundle.getDividendYield()));
    return costOfCarry * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the discount rate
   */
  @Override
  public double ratesDelta(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getFundingCurve());
    Validate.notNull(dataBundle.getSpotValue());
    Validate.notNull(dataBundle.getDividendYield());

    double timeToExpiry = future.getTimeToSettlement();
    double discountRate = dataBundle.getFundingCurve().getInterestRate(timeToExpiry);
    double costOfCarry = Math.exp(timeToExpiry * (discountRate - dataBundle.getDividendYield()));
    double fwdPrice = dataBundle.getSpotValue() * costOfCarry;
    return timeToExpiry * fwdPrice * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a basis point change in the discount rate
   */
  @Override
  public double pv01(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    return ratesDelta(future, dataBundle) / 10000;
  }
}
