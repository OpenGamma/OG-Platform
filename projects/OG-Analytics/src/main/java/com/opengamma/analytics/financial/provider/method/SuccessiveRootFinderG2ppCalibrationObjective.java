/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.g2pp.PresentValueG2ppCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for Hull-White model calibration with cap/floor.
 */
public class SuccessiveRootFinderG2ppCalibrationObjective extends SuccessiveRootFinderCalibrationObjectiveWithMultiCurves {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final PresentValueG2ppCalculator PVG2C = PresentValueG2ppCalculator.getInstance();

  /**
   * The ratio between the first factor volatility and the second factor volatility.
   */
  private final double _ratio;
  /**
   * The G2++ parameters before calibration. The calibration is done on the last volatility.
   */
  private final G2ppPiecewiseConstantParameters _g2Parameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyG2;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private G2ppProviderInterface _g2Provider;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   * @param ccy The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   * @param ratio The ratio between the first factor volatility and the second factor volatility.
   */
  public SuccessiveRootFinderG2ppCalibrationObjective(final G2ppPiecewiseConstantParameters parameters, final Currency ccy, final double ratio) {
    super(new FXMatrix(ccy), ccy);
    _g2Parameters = parameters;
    _ratio = ratio;
    _ccyG2 = ccy;
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
    _g2Provider = new G2ppProvider(multicurves, _g2Parameters, _ccyG2);
  }

  /**
   * Gets the G2++ data.
   * @return The G2++ data.
   */
  public G2ppPiecewiseConstantParameters getG2Parameters() {
    return _g2Parameters;
  }

  /**
   * Sets the Hull-White curve bundle.
   * @return The Hull-White curve bundle.
   */
  public G2ppProviderInterface getG2Provider() {
    return _g2Provider;
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
  }

  @Override
  public Double evaluate(Double x) {
    _g2Provider.getG2ppParameters().setLastVolatilities(new double[] {x, x / _ratio });
    return _g2Provider.getMulticurveProvider().getFxRates().convert(getInstrument().accept(PVG2C, _g2Provider), _ccyG2).getAmount() - getPrice();
  }

}
