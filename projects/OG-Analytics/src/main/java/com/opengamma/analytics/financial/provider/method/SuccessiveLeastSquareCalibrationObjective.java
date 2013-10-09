/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A function used as objective function for calibration on successive instrument with a least square process for each of them.
 * The least square is done on the prices.
 */
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
   * The exchange rate to convert the present values in a unique currency.
   */
  private final FXMatrix _fxMatrix;
  /**
   * The unique currency in which all present values are converted.
   */
  private final Currency _ccy;

  /**
   * Constructor.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public SuccessiveLeastSquareCalibrationObjective(FXMatrix fxMatrix, Currency ccy) {
    _fxMatrix = fxMatrix;
    _ccy = ccy;
  }

  /**
   * Gets the fxMatrix field.
   * @return the fxMatrix
   */
  public FXMatrix getFXMatrix() {
    return _fxMatrix;
  }

  /**
   * Gets the ccy field.
   * @return the ccy
   */
  public Currency getCcy() {
    return _ccy;
  }

  /**
   * Sets the instruments to calibrate.
   * @param instruments The instruments.
   */
  public void setInstruments(InstrumentDerivative[] instruments) {
    _instruments = instruments;
  }

  /**
   * Sets the prices of the instruments to calibrate. The instruments should be set first.
   * @param prices The prices.
   */
  public void setPrice(double[] prices) {
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
}
