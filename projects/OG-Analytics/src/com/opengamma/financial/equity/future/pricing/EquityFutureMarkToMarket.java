/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.future.EquityFutureDataBundle;
import com.opengamma.financial.equity.future.derivative.EquityFuture;

/**
 * Method to compute a future's present value given market price.
 * FIXME !!! This probably needs to be discounted by daysToSpot...(depends on margining behaviour) curve.getDiscountFactor(future.getDaysToSpot())
 */
public final class EquityFutureMarkToMarket implements EquityFuturesPricer {

  private EquityFutureMarkToMarket() {
  }

  private static final EquityFutureMarkToMarket INSTANCE = new EquityFutureMarkToMarket();

  /**
   * @return singleton instance of this pricing method
   */
  public static EquityFutureMarkToMarket getInstance() {
    return INSTANCE;
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
    Validate.notNull(dataBundle.getMarketPrice());
    return (dataBundle.getMarketPrice() - future.getStrike()) * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the underlying's spot value
   */
  @Override
  public double spotDelta(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    return future.getUnitAmount();
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
    Validate.notNull(dataBundle.getMarketPrice());
    return future.getTimeToSettlement() * dataBundle.getMarketPrice() * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a basis point change in the discount rate
   */
  @Override
  public double PV01(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    return ratesDelta(future, dataBundle) / 10000;
  }
}
