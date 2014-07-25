/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

/**
 * 
 */
public interface CapletStripper {

  /**
   * Solve directly for prices. This can weight the solution towards the more expensive caps (i.e. in-the-money & long dated).
   * The chi-square will be the sum of squared between (fitted) model and market prices  
   * @param pricer A {@link MultiCapFloorPricer} that contains the caps broken down into caplets 
   * @param capPrices the market prices of the caps 
   * @return The result of the stripping routine. 
   */
  CapletStrippingResult solveForPrice(MultiCapFloorPricer pricer, double[] capPrices);

  /**
   * Solve for weighted cap prices; the weights are the inverse of the supplied errors.   The chi-square will be the sum of squared
   * of  <i>modelPrice - marketPrice)/error</i>. A common strategy is to use the cap vega as the error. 
   * @param pricer A {@link MultiCapFloorPricer} that contains the caps broken down into caplets 
   * @param capPrices the market prices of the caps
   * @param errors The errors (price tolerance)
   * @return The result of the stripping routine.
   */
  CapletStrippingResult solveForPrice(MultiCapFloorPricer pricer, double[] capPrices, double[] errors);

  /**
   * Solve for cap implied volatility. This can often take much longer than solving for price. 
   * @param pricer A {@link MultiCapFloorPricer} that contains the caps broken down into caplets
   * @param capVol the market implied volatilities of the caps
   * @return The result of the stripping routine
   */
  CapletStrippingResult solveForVol(MultiCapFloorPricer pricer, double[] capVol);

}
