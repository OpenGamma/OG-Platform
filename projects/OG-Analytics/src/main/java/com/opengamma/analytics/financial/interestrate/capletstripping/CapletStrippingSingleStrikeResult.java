/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Holds the results of performing caplet stripping on a set of caps (on the same Ibor index) <b>with the same strike</b>
 */
public class CapletStrippingSingleStrikeResult {

  private final VolatilitySurface _volatilitySurface;
  private final double _chiSq;
  private final DoubleMatrix1D _fitParameters;
  private final DoubleMatrix1D _modelPrices;

  /**
   *
   * @param chiSq The chi-square of the fit. This will be zero (to the stopping tolerance) for root finding based strippers
   * @param fitParms The fit parameters from running the stripping routine. The model prices and volatility curve are derived from the fitted parameters via the
   * model used in the stripping routine.
   * @param volCurve The volatility curve for the caplets that <i>best</i> reproduces the model cap prices
   * @param modelPrices The cap prices produced by the stripping - these will be identical (to within tolerance) to the market prices for root finding based
  * routines, but could differ for least-squares
   */
  public CapletStrippingSingleStrikeResult(final double chiSq, final DoubleMatrix1D fitParms, final VolatilitySurface volCurve, final DoubleMatrix1D modelPrices) {
    ArgumentChecker.isTrue(chiSq >= 0, "Negative chiSq");
    ArgumentChecker.notNull(fitParms, "null fit parameters");
    ArgumentChecker.notNull(volCurve, "null vol curve");
    ArgumentChecker.notNull(modelPrices, "null model prices");
    _chiSq = chiSq;
    _fitParameters = fitParms;
    _modelPrices = modelPrices;
    _volatilitySurface = volCurve;
  }

  /**
   * Gets the volatilityCurve.
   * @return the volatilityCurve
   */
  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

  /**
   * Gets the chiSq.
   * @return the chiSq
   */
  public double getChiSq() {
    return _chiSq;
  }

  /**
   * Gets the fitParameters.
   * @return the fitParameters
   */
  public DoubleMatrix1D getFitParameters() {
    return _fitParameters;
  }

  /**
   * Gets the modelValues.
   * @return the modelValues
   */
  public DoubleMatrix1D getModelValues() {
    return _modelPrices;
  }

}
