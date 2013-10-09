/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.provider.calculator.libormarket.PresentValueLMMDDCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for LMM calibration with swaptions. The calibration is done on the volatilities and the displacements (skews).
 */
public class SuccessiveLeastSquareLMMDDCalibrationObjective extends SuccessiveLeastSquareCalibrationObjectiveWithMultiCurves {

  /**
   * The pricing method used to price the swaptions.
   */
  private static final PresentValueLMMDDCalculator PVLMC = PresentValueLMMDDCalculator.getInstance();
  /**
   * The LMM parameters. The calibration is done on the block of parameters between _startIndex and _endIndex.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _lmmParameters;
  /**
   * The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  private final Currency _ccyLMM;
  /**
   * The LMM parameters and curves bundle.
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
   * The initial displacement parameters before calibration.
   */
  private final double[] _displacementInit;

  /**
   * Constructor of the objective function with the LMM parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-1; maximum: 1.0E+1, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   * @param ccy The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  public SuccessiveLeastSquareLMMDDCalibrationObjective(final LiborMarketModelDisplacedDiffusionParameters parameters, final Currency ccy) {
    super(new FXMatrix(ccy), ccy);
    _lmmParameters = parameters;
    _ccyLMM = ccy;
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

  @Override
  public void setMulticurves(MulticurveProviderInterface multicurves) {
    _lmmProvider = new LiborMarketModelDisplacedDiffusionProvider(multicurves, _lmmParameters, _ccyLMM);
  }

  /**
   * Gets the LMM data.
   * @return The LMM data.
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
  /**
   * The inputs are the multiplicative factor on the volatilities and the additive term on the displacement.
   */
  public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
    int nbVol = _endIndex - _startIndex + 1;
    double[][] volChanged = new double[nbVol][_lmmParameters.getNbFactor()];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      for (int loopfact = 0; loopfact < _lmmParameters.getNbFactor(); loopfact++) {
        volChanged[loopperiod][loopfact] = _volatilityInit[loopperiod + _startIndex][loopfact] * x.getEntry(0);
      }
    }
    _lmmParameters.setVolatility(volChanged, _startIndex);
    double[] disChanged = new double[nbVol];
    for (int loopperiod = 0; loopperiod < nbVol; loopperiod++) {
      disChanged[loopperiod] = _displacementInit[loopperiod + _startIndex] + x.getEntry(1);
    }
    _lmmParameters.setDisplacement(disChanged, _startIndex);
    int nbInstruments = getInstruments().length;
    Double[] result = new Double[nbInstruments];
    // Implementation note: The pv error for each instrument 
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      result[loopins] = getInstruments()[loopins].accept(PVLMC, _lmmProvider).getAmount(_ccyLMM) - getPrices()[loopins];
    }
    return new DoubleMatrix1D(result);
  }

}
