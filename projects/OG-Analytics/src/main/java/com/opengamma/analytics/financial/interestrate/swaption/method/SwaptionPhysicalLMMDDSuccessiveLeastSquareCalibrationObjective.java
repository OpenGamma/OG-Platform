/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveLeastSquareCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Specific objective function for LMM calibration with swaptions. The calibration is done on the volatilities and the displacements (skews).
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationObjective extends SuccessiveLeastSquareCalibrationObjective {

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
   * The initial displacement parameters before calibration.
   */
  private final double[] _displacementInit;

  /**
   * Constructor of the objective function with the LMM parameters. The parameters range and accuracy are set at some default value
   * (minimum: 1.0E-1; maximum: 1.0E+1, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   */
  public SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationObjective(final LiborMarketModelDisplacedDiffusionParameters parameters) {
    ArgumentChecker.notNull(parameters, "parameters");
    _lmmParameters = parameters;
    _volatilityInit = new double[_lmmParameters.getNbPeriod()][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < _lmmParameters.getNbPeriod(); loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        _volatilityInit[loopperiod][loopfact] = parameters.getVolatility()[loopperiod][loopfact];
      }
    }
    _displacementInit = new double[_lmmParameters.getNbPeriod()];
    for (int loopperiod = 0; loopperiod < _lmmParameters.getNbPeriod(); loopperiod++) {
      _displacementInit[loopperiod] = parameters.getDisplacement()[loopperiod];
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
  /**
   * The inputs are the multiplicative factor on the volatilities and the additive term on the displacement.
   */
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    final int nbVol = _endIndex - _startIndex + 1;
    final double[][] volChanged = new double[nbVol][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        volChanged[loopperiod][loopfact] = _volatilityInit[loopperiod + _startIndex][loopfact] * x.getEntry(0);
      }
    }
    _lmmParameters.setVolatility(volChanged, _startIndex);
    final double[] disChanged = new double[nbVol];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      disChanged[loopperiod] = _displacementInit[loopperiod + _startIndex] + x.getEntry(1);
    }
    _lmmParameters.setDisplacement(disChanged, _startIndex);
    final int nbInstruments = getInstruments().length;
    final Double[] result = new Double[nbInstruments];
    // Implementation note: The pv error for each instrument
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      result[loopins] = METHOD_LMM_SWAPTION.presentValue((SwaptionPhysicalFixedIbor) getInstruments()[loopins], _lmmBundle).getAmount() - getPrices()[loopins];
    }
    return new DoubleMatrix1D(result);
  }

}
