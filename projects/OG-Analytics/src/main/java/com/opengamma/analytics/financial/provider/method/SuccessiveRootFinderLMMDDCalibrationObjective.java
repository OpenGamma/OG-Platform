/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.provider.calculator.libormarket.PresentValueLMMDDCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for Hull-White model calibration with cap/floor.
 */
public class SuccessiveRootFinderLMMDDCalibrationObjective extends SuccessiveRootFinderCalibrationObjectiveWithMultiCurves {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final PresentValueLMMDDCalculator PVLMC = PresentValueLMMDDCalculator.getInstance();
  /**
   * The Hull-White parameters before calibration. The calibration is done on the last volatility.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _lmmParameters;
  /**
   * The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  private final Currency _ccyLMM;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private LiborMarketModelDisplacedDiffusionProvider _lmmProvider;
  /**
   * The start index for the calibration.
   */
  private int _startIndex;
  /**
   * The end index for the calibration.
   */
  private int _endIndex;
  /**
   * The initial LMM volatilities before calibration.
   */
  private final double[][] _volatilityInit;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   * @param ccy The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  public SuccessiveRootFinderLMMDDCalibrationObjective(final LiborMarketModelDisplacedDiffusionParameters parameters, final Currency ccy) {
    super(new FXMatrix(ccy), ccy);
    _lmmParameters = parameters;
    _ccyLMM = ccy;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
    _volatilityInit = new double[_lmmParameters.getNbPeriod()][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < _lmmParameters.getNbPeriod(); loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        _volatilityInit[loopperiod][loopfact] = parameters.getVolatility()[loopperiod][loopfact];
      }
    }
  }

  /**
   * Sets the Hull-White curve bundle using the Hull-White parameters and a given set of curves.
   * @param multicurves The multi-curves provider.
   */
  @Override
  public void setMulticurves(MulticurveProviderInterface multicurves) {
    _lmmProvider = new LiborMarketModelDisplacedDiffusionProvider(multicurves, _lmmParameters, _ccyLMM);
  }

  /**
   * Gets the Hull-White data.
   * @return The Hull-White data.
   */
  public LiborMarketModelDisplacedDiffusionParameters getLMMParameters() {
    return _lmmParameters;
  }

  /**
   * Sets the LMM.
   * @return The LMM.
   */
  public LiborMarketModelDisplacedDiffusionProvider getLMMProvider() {
    return _lmmProvider;
  }

  @Override
  public void setInstrument(InstrumentDerivative instrument) {
    super.setInstrument(instrument);
  }

  /**
   * Gets the start index.
   * @return The start index.
   */
  public int getStartIndex() {
    return _startIndex;
  }

  /**
   * Sets the start index.
   * @param startIndex The start index.
   */
  public void setStartIndex(int startIndex) {
    _startIndex = startIndex;
  }

  /**
   * Gets the end index.
   * @return The end index.
   */
  public int getEndIndex() {
    return _endIndex;
  }

  /**
   * Sets the end index.
   * @param endIndex The end index.
   */
  public void setEndIndex(int endIndex) {
    _endIndex = endIndex;
  }

  /**
   * Gets the initial volatility array.
   * @return The initial volatility array.
   */
  public double[][] getVolatilityInit() {
    return _volatilityInit;
  }

  @Override
  public Double evaluate(Double x) {
    int nbVol = _endIndex - _startIndex + 1;
    double[][] volChanged = new double[nbVol][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        volChanged[loopperiod][loopfact] = _volatilityInit[loopperiod + _startIndex][loopfact] * x;
      }
    }
    _lmmParameters.setVolatility(volChanged, _startIndex);
    return getInstrument().accept(PVLMC, _lmmProvider).getAmount(_ccyLMM) - getPrice();
  }

}
