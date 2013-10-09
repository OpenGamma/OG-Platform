/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * A function used as objective function for calibration on successive instrument with a least square process for each of them.
 * The least square is done on the prices.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class SuccessiveLeastSquareCalibrationObjective extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * The instruments to be calibrated.
   */

  private InstrumentDerivative[] _instruments;

  /**
    * The instruments prices.
    */

  private double[] _prices;

  /**
    * Sets the instruments to calibrate.
    * @param instruments The instruments.
    */

  public void setInstruments(final InstrumentDerivative[] instruments) {
    _instruments = instruments;
  }

  /**
    * Sets the prices of the instruments to calibrate. The instruments should be set first.
    * @param prices The prices.
    */

  public void setPrice(final double[] prices) {
    ArgumentChecker.isTrue(prices.length == _instruments.length, "Incorrect number of prices.");
    _prices = prices;
  }

  /**
    * Gets the prices.
    * @return The prices.
    */

  public double[] getPrices() {
    return _prices;
  }

  /**
    * Gets the instruments.
    * @return The instruments.
    */

  public InstrumentDerivative[] getInstruments() {
    return _instruments;
  }

  /**
    * Sets the curve bundle used in the calibration.
    * @param curves The curves.
    */

  public abstract void setCurves(YieldCurveBundle curves);

}
