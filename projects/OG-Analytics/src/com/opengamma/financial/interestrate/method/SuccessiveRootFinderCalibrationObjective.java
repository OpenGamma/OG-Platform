/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.math.function.Function1D;

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

  //TODO: review the default values.

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

  /**
   * Sets the curve bundle used in the calibration.
   * @param curves The curves.
   */
  public abstract void setCurves(YieldCurveBundle curves);

}
