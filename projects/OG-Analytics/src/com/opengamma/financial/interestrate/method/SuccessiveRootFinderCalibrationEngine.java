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
import com.opengamma.math.rootfinding.RidderSingleRootFinder;

/**
 * Calibration engine calibrating successively the instruments in the basket trough a root-finding process.
 */
public abstract class SuccessiveRootFinderCalibrationEngine extends CalibrationEngine {

  /**
   * The calibration objective.
   */
  private final SuccessiveRootFinderCalibrationObjective _calibrationObjective;
  /**
   * The list of calibration times.
   */
  private final List<Double> _calibrationTimes = new ArrayList<Double>();

  public SuccessiveRootFinderCalibrationEngine(final SuccessiveRootFinderCalibrationObjective calibrationObjective) {
    _calibrationObjective = calibrationObjective;
  }

  /**
   * Gets the calibration times list.
   * @return The calibration times.
   */
  public List<Double> getCalibrationTimes() {
    return _calibrationTimes;
  }

  @Override
  public void calibrate(YieldCurveBundle curves) {
    computeCalibrationPrice(curves);
    _calibrationObjective.setCurves(curves);
    int nbInstruments = getBasket().size();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(_calibrationObjective.getFunctionValueAccuracy(), _calibrationObjective.getVariableAbsoluteAccuracy());
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      InterestRateDerivative instrument = getBasket().get(loopins);
      _calibrationObjective.setInstrument(instrument);
      _calibrationObjective.setPrice(getCalibrationPrice().get(loopins));
      rootFinder.getRoot(_calibrationObjective, _calibrationObjective.getMinimumParameter(), _calibrationObjective.getMaximumParameter());
      if (loopins < nbInstruments - 1) {
        _calibrationObjective.setNextCalibrationTime(_calibrationTimes.get(loopins));
      }
    }
  }

}
