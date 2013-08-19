/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.method;

/**
 * Calibration engine calibrating successively the instruments in the basket trough a root-finding process.
 * @deprecated {@link SuccessiveLeastSquareCalibrationObjective} is deprecated
 */
@Deprecated
public abstract class SuccessiveLeastSquareCalibrationEngine extends CalibrationEngine {

  /**
   * The calibration objective.
   */
  private final SuccessiveLeastSquareCalibrationObjective _calibrationObjective;

  /**
   * The constructor.
   * @param calibrationObjective The calibration objective.
   */
  public SuccessiveLeastSquareCalibrationEngine(final SuccessiveLeastSquareCalibrationObjective calibrationObjective) {
    _calibrationObjective = calibrationObjective;
  }

  /**
   * Gets the calibration objective.
   * @return The calibration objective.
   */
  public SuccessiveLeastSquareCalibrationObjective getCalibrationObjective() {
    return _calibrationObjective;
  }

}
