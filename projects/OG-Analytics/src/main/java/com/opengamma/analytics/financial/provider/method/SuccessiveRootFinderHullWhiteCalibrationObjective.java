/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for Hull-White model calibration with cap/floor.
 */
public class SuccessiveRootFinderHullWhiteCalibrationObjective extends SuccessiveRootFinderCalibrationObjectiveWithMultiCurves {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  /**
   * The Hull-White parameters before calibration. The calibration is done on the last volatility.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _hwParameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyHW;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private HullWhiteOneFactorProvider _hwProvider;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   * @param ccy The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  public SuccessiveRootFinderHullWhiteCalibrationObjective(final HullWhiteOneFactorPiecewiseConstantParameters parameters, final Currency ccy) {
    super(new FXMatrix(ccy), ccy);
    _hwParameters = parameters;
    _ccyHW = ccy;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
  }

  /**
   * Sets the Hull-White curve bundle using the Hull-White parameters and a given set of curves.
   * @param multicurves The multi-curves provider.
   */
  @Override
  public void setMulticurves(MulticurveProviderInterface multicurves) {
    _hwProvider = new HullWhiteOneFactorProvider(multicurves, _hwParameters, _ccyHW);
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
  public HullWhiteOneFactorProvider getHwProvider() {
    return _hwProvider;
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
  }

  @Override
  public Double evaluate(Double x) {
    _hwProvider.getHullWhiteParameters().setLastVolatility(x);
    return _hwProvider.getMulticurveProvider().getFxRates().convert(getInstrument().accept(PVHWC, _hwProvider), _ccyHW).getAmount() - getPrice();
  }

}
