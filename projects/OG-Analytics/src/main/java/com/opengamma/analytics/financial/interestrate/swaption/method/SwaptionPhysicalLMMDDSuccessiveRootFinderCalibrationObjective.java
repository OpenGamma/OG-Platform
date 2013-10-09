/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Specific objective function for LMM calibration with swaptions.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective extends SuccessiveRootFinderCalibrationObjective {

  /**
   * The pricing method used to price the swaptions.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM_SWAPTION = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  /**
   * The LMM parameters. The calibration is done on the block of parameters between _startIndex and _endIndex.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _lmmParameters;
  /**
   * The LMM parameters and curves bundle.
   */
  private LiborMarketModelDisplacedDiffusionDataBundle _lmmBundle;
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
   * Constructor of the objective function with the LMM parameters. The parameters range and accuracy are set at some default value
   * (minimum: 1.0E-1; maximum: 1.0E+1, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   */
  public SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective(final LiborMarketModelDisplacedDiffusionParameters parameters) {
    ArgumentChecker.notNull(parameters, "parameters");
    _lmmParameters = parameters;
    setMinimumParameter(1.0E-1);
    setMaximumParameter(1.0E+1);
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
   * Gets the LMM data.
   * @return The LMM data.
   */
  public LiborMarketModelDisplacedDiffusionParameters getLMMParameters() {
    return _lmmParameters;
  }

  /**
   * Gets the LMM curve bundle.
   * @return The LMM curve bundle.
   */
  public LiborMarketModelDisplacedDiffusionDataBundle getLMMBundle() {
    return _lmmBundle;
  }

  @Override
  public void setCurves(final YieldCurveBundle curves) {
    _lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(_lmmParameters, curves);
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
  public void setStartIndex(final int startIndex) {
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
  public void setEndIndex(final int endIndex) {
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
  public Double evaluate(final Double x) {
    final int nbVol = _endIndex - _startIndex + 1;
    final double[][] volChanged = new double[nbVol][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        volChanged[loopperiod][loopfact] = _volatilityInit[loopperiod + _startIndex][loopfact] * x;
      }
    }
    _lmmParameters.setVolatility(volChanged, _startIndex);
    return METHOD_LMM_SWAPTION.presentValue((SwaptionPhysicalFixedIbor) getInstrument(), _lmmBundle).getAmount() - getPrice();
  }

}
