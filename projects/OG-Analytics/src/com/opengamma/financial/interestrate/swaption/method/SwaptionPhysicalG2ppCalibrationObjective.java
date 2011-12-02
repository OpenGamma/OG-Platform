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
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

/**
 * Specific objective function for G2++ model calibration with swaptions (both volatilities are calibrated to the same swaption by imposing a ratio between the volatilities).
 */
public class SwaptionPhysicalG2ppCalibrationObjective extends SuccessiveRootFinderCalibrationObjective {

  private final double _ratio;
  /**
   * The G2++ parameters before calibration. The calibration is done on the last volatility.
   */
  private final G2ppPiecewiseConstantParameters _g2Parameters;
  /**
   * The G2++ parameters and curves bundle.
   */
  private G2ppPiecewiseConstantDataBundle _g2Bundle;
  /**
   * The cash flow equivalent of the instrument (swaption) to calibrate.
   */
  private AnnuityPaymentFixed _cfe;
  /**
   * The pricing method used to price the swaptions.
   */
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2_SWAPTION = new SwaptionPhysicalFixedIborG2ppApproximationMethod();
  /**
   * The cash-flow equivalent method used in the calibration.
   */
  private static final CashFlowEquivalentCalculator CASH_FLOW_EQUIVALENT_CALCULATOR = CashFlowEquivalentCalculator.getInstance();

  /**
   * Constructor of the objective function with the G2++ parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The G2++ parameters.
   * @param ratio The ratio between the first factor volatility and the second factor volatility.
   */
  public SwaptionPhysicalG2ppCalibrationObjective(final G2ppPiecewiseConstantParameters parameters, double ratio) {
    _g2Parameters = parameters;
    _ratio = ratio;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
  }

  /**
   * Sets the G2++ curve bundle using the Hull-White parameters and a given set of curves.
   * @param curves The curves.
   */
  @Override
  public void setCurves(YieldCurveBundle curves) {
    _g2Bundle = new G2ppPiecewiseConstantDataBundle(_g2Parameters, curves);
  }

  /**
   * Gets the G2++ data.
   * @return The G2++ data.
   */
  public G2ppPiecewiseConstantParameters getG2Parameters() {
    return _g2Parameters;
  }

  /**
   * Sets the G2++ curve bundle.
   * @return The G2++ curve bundle.
   */
  public G2ppPiecewiseConstantDataBundle getG2Bundle() {
    return _g2Bundle;
  }

  /**
   * Sets the calibration time for the next calibration.
   * @param calibrationTime The calibration time.
   */
  public void setNextCalibrationTime(double calibrationTime) {
    _g2Parameters.addVolatility(_g2Parameters.getLastVolatilities(), calibrationTime);
  }

  @Override
  public void setInstrument(InstrumentDerivative instrument) {
    super.setInstrument(instrument);
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Instrument should be a physical delivery swaption");
    _cfe = CASH_FLOW_EQUIVALENT_CALCULATOR.visit(((SwaptionPhysicalFixedIbor) instrument).getUnderlyingSwap(), _g2Bundle);
  }

  @Override
  public Double evaluate(Double x) {
    _g2Bundle.getG2ppParameter().setLastVolatilities(new double[] {x, x / _ratio});
    return METHOD_G2_SWAPTION.presentValue((SwaptionPhysicalFixedIbor) getInstrument(), _cfe, _g2Bundle).getAmount() - getPrice();
  }

}
