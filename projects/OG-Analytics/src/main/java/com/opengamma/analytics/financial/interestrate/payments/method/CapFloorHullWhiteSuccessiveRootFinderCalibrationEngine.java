/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;

/**
 * Specific calibration engine for the Hull-White one factor model with cap/floor.
 * @deprecated {@link PricingMethod} is deprecated.
 */
@Deprecated
public class CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * The list of calibration times.
   */
  private final List<Double> _calibrationTimes = new ArrayList<>();

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine(final SuccessiveRootFinderCalibrationObjective calibrationObjective) {
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
    for (final InstrumentDerivative element : instrument) {
      Validate.isTrue(element instanceof CapFloorIbor, "Calibration instruments should be cap/floor");
      getBasket().add(element);
      getMethod().add(method);
      getCalibrationPrice().add(0.0);
      _calibrationTimes.add(((CapFloorIbor) element).getFixingTime());
    }
  }

  @Override
  public void calibrate(final YieldCurveBundle curves) {
    computeCalibrationPrice(curves);
    getCalibrationObjective().setCurves(curves);
    final int nbInstruments = getBasket().size();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(getCalibrationObjective().getFunctionValueAccuracy(), getCalibrationObjective().getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      final InstrumentDerivative instrument = getBasket().get(loopins);
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
