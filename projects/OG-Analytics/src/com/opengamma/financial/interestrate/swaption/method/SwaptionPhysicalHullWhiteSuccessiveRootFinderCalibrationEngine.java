/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Specific calibration engine for the Hull-White one factor model with swaption.
 */
public class SwaptionPhysicalHullWhiteSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SwaptionPhysicalHullWhiteSuccessiveRootFinderCalibrationEngine(SuccessiveRootFinderCalibrationObjective calibrationObjective) {
    super(calibrationObjective);
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param method A calculator.
   */
  @Override
  public void addInstrument(final InterestRateDerivative instrument, final PricingMethod method) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Calibration instruments should be swaptions");
    getBasket().add(instrument);
    getMethod().add(method);
    getCalibrationPrice().add(0.0);
    getCalibrationTimes().add(((SwaptionPhysicalFixedIbor) instrument).getTimeToExpiry());
  }

}
