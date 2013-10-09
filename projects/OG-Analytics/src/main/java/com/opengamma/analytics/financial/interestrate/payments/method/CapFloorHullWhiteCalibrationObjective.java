/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;

/**
 * Specific objective function for Hull-White model calibration with cap/floor.
 * @deprecated {@link HullWhiteOneFactorPiecewiseConstantDataBundle} is deprecated
 */
@Deprecated
public class CapFloorHullWhiteCalibrationObjective extends SuccessiveRootFinderCalibrationObjective {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final CapFloorIborHullWhiteMethod METHOD_HW_CAP = new CapFloorIborHullWhiteMethod();
  /**
   * The Hull-White parameters before calibration. The calibration is done on the last volatility.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _hwParameters;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private HullWhiteOneFactorPiecewiseConstantDataBundle _hwBundle;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   */
  public CapFloorHullWhiteCalibrationObjective(final HullWhiteOneFactorPiecewiseConstantParameters parameters) {
    _hwParameters = parameters;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
  }

  /**
   * Sets the Hull-White curve bundle using the Hull-White parameters and a given set of curves.
   * @param curves The curves.
   */
  @Override
  public void setCurves(final YieldCurveBundle curves) {
    _hwBundle = new HullWhiteOneFactorPiecewiseConstantDataBundle(_hwParameters, curves);
  }

  /**
   * Gets the Hull-White data.
   * @return The Hull-White data.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters getHwParameters() {
    return _hwParameters;
  }

  /**
   * Sets the Hull-White curve bundle.
   * @return The Hull-White curve bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle getHwBundle() {
    return _hwBundle;
  }

  /**
   * Sets the calibration time for the next calibration.
   * @param calibrationTime The calibration time.
   */
  public void setNextCalibrationTime(final double calibrationTime) {
    _hwParameters.addVolatility(_hwParameters.getLastVolatility(), calibrationTime);
  }

  @Override
  public void setInstrument(final InstrumentDerivative instrument) {
    super.setInstrument(instrument);
    Validate.isTrue(instrument instanceof CapFloorIbor, "Instrument should be a cap/floor");
  }

  @Override
  public Double evaluate(final Double x) {
    _hwBundle.getHullWhiteParameter().setLastVolatility(x);
    return METHOD_HW_CAP.presentValue(getInstrument(), _hwBundle).getAmount() - getPrice();
  }

}
