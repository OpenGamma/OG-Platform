/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Specific calibration engine for the G2++ model with swaption.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class SwaptionPhysicalG2ppSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * The list of calibration times.
   */
  private final List<Double> _calibrationTimes = new ArrayList<>();

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SwaptionPhysicalG2ppSuccessiveRootFinderCalibrationEngine(final SuccessiveRootFinderCalibrationObjective calibrationObjective) {
    super(calibrationObjective);
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param method A calculator.
   */
  @Override
  public void addInstrument(final InstrumentDerivative instrument, final PricingMethod method) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Calibration instruments should be swaptions");
    getBasket().add(instrument);
    getMethod().add(method);
    getCalibrationPrice().add(0.0);
    _calibrationTimes.add(((SwaptionPhysicalFixedIbor) instrument).getTimeToExpiry());
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
        ((SwaptionPhysicalG2ppCalibrationObjective) getCalibrationObjective()).setNextCalibrationTime(_calibrationTimes.get(loopins));
      }
    }
  }

}
