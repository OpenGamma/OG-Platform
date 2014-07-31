/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public interface CapletStripper {

  /**
   * Solve directly for the market values - these are either cap prices or cap (implied) volatilities.  
   * The chi-square will be the sum of squared differences between (calibrated) model and market values. 
   * If used with prices, this can weight the solution towards the more expensive caps (i.e. in-the-money & long dated).
   * @param marketValues Quoted market values (prices or implied volatilities) of the cap/floors 
   * @param type Are the market values prices or volatilities 
   * @return The result of the stripping routine
   */
  CapletStrippingResult solve(double[] marketValues, MarketDataType type);

  /**
   * Solve for weighted market values - these are either cap prices or cap (implied) volatilities.  
   * The chi-square will be the sum of squared differences between (calibrated) model and market values, divided by the error;
   * if the error is set to something meaningful (like the bid-offer), the chi-square value has some meaning (rather than
   * just being a metric that is minimised. 
   * @param marketValues Quoted market values (prices or implied volatilities)
  * @param type Are the market values prices or volatilities 
   * @param errors The expected difference between model and market values 
   * @return The result of the stripping routine
   */
  CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors);

  /**
   * Solve directly for the market values - these are either cap prices or cap (implied) volatilities.  
   * The chi-square will be the sum of squared differences between (calibrated) model and market values. 
   * If used with prices, this can weight the solution towards the more expensive caps (i.e. in-the-money & long dated).
   * @param marketValues Quoted market values (prices or implied volatilities) of the cap/floors 
   * @param type Are the market values prices or volatilities 
   * @param guess Starting values of the model parameters used by the optimiser 
   * @return  The result of the stripping routine
   */
  CapletStrippingResult solve(double[] marketValues, MarketDataType type, DoubleMatrix1D guess);

  /**
   * Solve for weighted market values - these are either cap prices or cap (implied) volatilities.  
   * The chi-square will be the sum of squared differences between (calibrated) model and market values, divided by the error;
   * if the error is set to something meaningful (like the bid-offer), the chi-square value has some meaning (rather than
   * just being a metric that is minimised. 
   * @param marketValues Quoted market values (prices or implied volatilities)
   * @param type Are the market values prices or volatilities 
   * @param errors The expected difference between model and market values 
   * @param guess Starting values of the model parameters used by the optimiser 
   * @return The result of the stripping routine
   */
  CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors, DoubleMatrix1D guess);

}
