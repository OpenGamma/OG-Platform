/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;

/**
 * Specific calibration engine for the Hull-White one factor model with cap/floor.
 */
public class CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * The list of calibration times.
   */
  private final List<Double> _calibrationTimes = new ArrayList<Double>();

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine(SuccessiveRootFinderCalibrationObjective calibrationObjective) {
    super(calibrationObjective);
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param method A pricing method.
   */
  @Override
  public void addInstrument(final InstrumentDerivative instrument, final PricingMethod method) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Calibration instruments should be cap/floor");
    getBasket().add(instrument);
    getMethod().add(method);
    getCalibrationPrice().add(0.0);
    _calibrationTimes.add(((CapFloorIbor) instrument).getFixingTime());
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param method A pricing method.
   */
  @Override
  public void addInstrument(final InstrumentDerivative[] instrument, final PricingMethod method) {
    for (int loopinstrument = 0; loopinstrument < instrument.length; loopinstrument++) {
      Validate.isTrue(instrument[loopinstrument] instanceof CapFloorIbor, "Calibration instruments should be cap/floor");
      getBasket().add(instrument[loopinstrument]);
      getMethod().add(method);
      getCalibrationPrice().add(0.0);
      _calibrationTimes.add(((CapFloorIbor) instrument[loopinstrument]).getFixingTime());
    }
  }

  @Override
  public void calibrate(YieldCurveBundle curves) {
    computeCalibrationPrice(curves);
    getCalibrationObjective().setCurves(curves);
    int nbInstruments = getBasket().size();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(getCalibrationObjective().getFunctionValueAccuracy(), getCalibrationObjective().getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      InstrumentDerivative instrument = getBasket().get(loopins);
      getCalibrationObjective().setInstrument(instrument);
      getCalibrationObjective().setPrice(getCalibrationPrice().get(loopins));
      final double[] range = bracketer.getBracketedPoints(getCalibrationObjective(), getCalibrationObjective().getMinimumParameter(), getCalibrationObjective().getMaximumParameter());
      rootFinder.getRoot(getCalibrationObjective(), range[0], range[1]);
      if (loopins < nbInstruments - 1) {
        ((CapFloorHullWhiteCalibrationObjective) getCalibrationObjective()).setNextCalibrationTime(_calibrationTimes.get(loopins));
      }
    }
  }

}
