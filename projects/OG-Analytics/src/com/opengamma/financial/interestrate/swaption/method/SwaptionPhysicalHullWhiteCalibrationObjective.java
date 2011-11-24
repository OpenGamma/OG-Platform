/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;

/**
 * Specific objective function for Hull-White model calibration with swaptions.
 */
public class SwaptionPhysicalHullWhiteCalibrationObjective extends SuccessiveRootFinderCalibrationObjective {

  /**
   * The pricing method used to price the swaptions.
   */
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW_SWAPTION = new SwaptionPhysicalFixedIborHullWhiteMethod();
  /**
   * The cash-flow equivalent method used in the calibration.
   */
  private static final CashFlowEquivalentCalculator CASH_FLOW_EQUIVALENT_CALCULATOR = CashFlowEquivalentCalculator.getInstance();
  /**
   * The Hull-White parameters before calibration. The calibration is done on the last volatility.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _hwParameters;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private HullWhiteOneFactorPiecewiseConstantDataBundle _hwBundle;
  /**
   * The cash flow equivalent of the instrument (swaption) to calibrate.
   */
  private AnnuityPaymentFixed _cfe;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   */
  public SwaptionPhysicalHullWhiteCalibrationObjective(final HullWhiteOneFactorPiecewiseConstantParameters parameters) {
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
  public void setCurves(YieldCurveBundle curves) {
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
  public void setNextCalibrationTime(double calibrationTime) {
    _hwParameters.addVolatility(_hwParameters.getLastVolatility(), calibrationTime);
  }

  @Override
  public void setInstrument(InstrumentDerivative instrument) {
    super.setInstrument(instrument);
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Instrument should be a physical delivery swaption");
    _cfe = CASH_FLOW_EQUIVALENT_CALCULATOR.visit(((SwaptionPhysicalFixedIbor) instrument).getUnderlyingSwap(), _hwBundle);
  }

  @Override
  public Double evaluate(Double x) {
    _hwBundle.getHullWhiteParameter().setLastVolatility(x);
    return METHOD_HW_SWAPTION.presentValue((SwaptionPhysicalFixedIbor) getInstrument(), _cfe, _hwBundle).getAmount() - getPrice();
  }

}
