/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Holds the results of performing caplet stripping on a set of caps (on the same Ibor index)
 */
public interface CapletStrippingResult {

  /**
   * Gets the chiSq.
   * @return the chiSq
   */
  double getChiSq();

  /**
   * Gets the fit parameters.
   * @return the fit parameters
   */
  DoubleMatrix1D getFitParameters();

  /**
   * Gets the model cap prices
   * @return the model cap prices
   */
  double[] getModelCapPrices();

  /**
   * Gets the model cap volatilities 
   * @return the model cap volatilities 
   */
  double[] getModelCapVols();

  /**
   * get the (fitted) caplet volatilities 
   * @return the caplet vols 
   */
  DoubleMatrix1D getCapletVols();

}
