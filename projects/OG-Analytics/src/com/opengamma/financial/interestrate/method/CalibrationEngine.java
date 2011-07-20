/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Generic calibration engine for interest rate instruments.
 */
public abstract class CalibrationEngine {

  /**
   * The calibration basket.
   */
  private final List<InterestRateDerivative> _basket;
  /**
   * The method used to compute calibrating prices.
   */
  private final List<PricingMethod> _method;
  /**
   * The calibrating prices.
   */
  private final List<Double> _calibrationPrice;

  //TODO: Should there exists also a way to add an instrument with its direct price (not the method)?

  /**
   * Constructor of the calibration engine. The basket and calculator list are empty.
   */
  public CalibrationEngine() {
    _basket = new ArrayList<InterestRateDerivative>();
    _method = new ArrayList<PricingMethod>();
    _calibrationPrice = new ArrayList<Double>();
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param method A calculator.
   */
  public void addInstrument(final InterestRateDerivative instrument, final PricingMethod method) {
    _basket.add(instrument);
    _method.add(method);
    _calibrationPrice.add(0.0);
  }

  /**
   * Computes the price of the instrument in the calibration basket using the engine calculator and the yield curves.
   * @param curves The curve bundle. Should contains all the data required by the calculators.
   */
  public void computeCalibrationPrice(YieldCurveBundle curves) {
    int nbInstrument = _basket.size();
    for (int loopins = 0; loopins < nbInstrument; loopins++) {
      double pv = _method.get(loopins).presentValue(_basket.get(loopins), curves).getAmount();
      _calibrationPrice.set(loopins, pv);
    }
  }

  /**
   * Calibrate the model using a given curve bundle.
   * @param curves The curves.
   */
  public abstract void calibrate(YieldCurveBundle curves);

  /**
   * Gets the instrument basket.
   * @return The basket.
   */
  public List<InterestRateDerivative> getBasket() {
    return _basket;
  }

  /**
   * Gets the method list.
   * @return the method.
   */
  public List<PricingMethod> getMethod() {
    return _method;
  }

  /**
   * Gets the _calibrationPrice field.
   * @return the _calibrationPrice
   */
  public List<Double> getCalibrationPrice() {
    return _calibrationPrice;
  }

}
