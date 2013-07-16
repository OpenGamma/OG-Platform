/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.money.Currency;

/**
 * A function used as objective function for calibration on successive instrument with a root finding process at each of them.
 */
public abstract class SuccessiveRootFinderCalibrationObjective extends Function1D<Double, Double> {

  /**
   * The instrument to be calibrated.
   */
  private InstrumentDerivative _instrument;
  /**
   * The instrument price.
   */
  private double _price;
  /**
   * The minimum parameter value for root finding. The default value is 1.0E-6;
   */
  private double _minimumParameter = 1.0E-6;
  /**
   * The maximum parameter value for root finding. The default value is 1.0;
   */
  private double _maximumParameter = 1.0;
  /**
   * The function absolute accuracy for root finding. The default value is 1.0E-6;
   */
  private double _functionValueAccuracy = 1.0E-6;
  /**
   * The parameter value absolute accuracy for root finding. The default value is 1.0E-8;
   */
  private double _variableAbsoluteAccuracy = 1.0E-8;
  /**
   * The exchange rate to convert the present values in a unique currency.
   */
  private final FXMatrix _fxMatrix;
  /**
   * The unique currency in which all present values are converted.
   */
  private final Currency _ccy;

  //TODO: review the default values.

  /**
   * Constructor.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public SuccessiveRootFinderCalibrationObjective(FXMatrix fxMatrix, Currency ccy) {
    _fxMatrix = fxMatrix;
    _ccy = ccy;
  }

  /**
   * Sets the instrument to calibrate.
   * @param instrument The instrument.
   */
  public void setInstrument(InstrumentDerivative instrument) {
    _instrument = instrument;
  }

  /**
   * Sets the price of the instrument to calibrate.
   * @param price The price.
   */
  public void setPrice(double price) {
    _price = price;
  }

  /**
   * Gets the price.
   * @return The price.
   */
  public double getPrice() {
    return _price;
  }

  /**
   * Gets the instrument.
   * @return The instrument.
   */
  public InstrumentDerivative getInstrument() {
    return _instrument;
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
   * Sets the minimum value of the parameter to calibrate.
   * @param minimumParameter The minimum value.
   */
  public void setMinimumParameter(double minimumParameter) {
    _minimumParameter = minimumParameter;
  }

  /**
   * Gets the minimum value of the parameter to calibrate.
   * @return The minimum value.
   */
  public double getMinimumParameter() {
    return _minimumParameter;
  }

  /**
   * Sets the maximum value of the parameter to calibrate.
   * @param maximumParameter The maximum value.
   */
  public void setMaximumParameter(double maximumParameter) {
    _maximumParameter = maximumParameter;
  }

  /**
   * Gets the maximum value of the parameter to calibrate.
   * @return The maximum value.
   */
  public double getMaximumParameter() {
    return _maximumParameter;
  }

  /**
   * Sets the function value accuracy of the calibration.
   * @param functionValueAccuracy The function value accuracy.
   */
  public void setFunctionValueAccuracy(double functionValueAccuracy) {
    _functionValueAccuracy = functionValueAccuracy;
  }

  /**
   * Gets the function value accuracy of the calibration.
   * @return The accuracy.
   */
  public double getFunctionValueAccuracy() {
    return _functionValueAccuracy;
  }

  /**
   * Sets the parameter absolute accuracy of the calibration.
   * @param variableAbsoluteAccuracy The parameter absolute accuracy.
   */
  public void setVariableAbsoluteAccuracy(double variableAbsoluteAccuracy) {
    _variableAbsoluteAccuracy = variableAbsoluteAccuracy;
  }

  /**
   * Gets the parameter absolute accuracy of the calibration.
   * @return The accuracy.
   */
  public double getVariableAbsoluteAccuracy() {
    return _variableAbsoluteAccuracy;
  }

}
