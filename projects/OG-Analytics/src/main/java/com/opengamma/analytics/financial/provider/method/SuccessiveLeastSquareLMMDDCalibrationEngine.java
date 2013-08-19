/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Specific calibration engine for the LMM model with swaption.
 * @param <DATA_TYPE>  The type of the data for the base calculator.
 */
public class SuccessiveLeastSquareLMMDDCalibrationEngine<DATA_TYPE extends ParameterProviderInterface> extends CalibrationEngineWithCalculators<DATA_TYPE> {

  /**
   * The precision used in least-square search.
   */
  private static final double DEFAULT_PRECISION = 1.0E-15;
  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentIndex = new ArrayList<>();
  /**
   * The number of instruments in a calibration block. The total number of instruments should be a multiple of that number.
   */
  private final int _nbInstrumentsBlock;

  /**
   * The calibration objective.
   */
  private final SuccessiveLeastSquareCalibrationObjectiveWithMultiCurves _calibrationObjective;

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   * @param nbInstrumentsBlock The number of instruments in a calibration block.
   */
  public SuccessiveLeastSquareLMMDDCalibrationEngine(final SuccessiveLeastSquareLMMDDCalibrationObjective calibrationObjective, final int nbInstrumentsBlock) {
    super(calibrationObjective.getFXMatrix(), calibrationObjective.getCcy());
    _instrumentIndex.add(0);
    _nbInstrumentsBlock = nbInstrumentsBlock;
    _calibrationObjective = calibrationObjective;
  }

  /**
   * Gets the instrument index.
   * @return The instrument index.
   */
  public List<Integer> getInstrumentIndex() {
    return _instrumentIndex;
  }

  /**
   * Returns the number of instruments in a calibration block.
   * @return The number.
   */
  public int getNbInstrumentsBlock() {
    return _nbInstrumentsBlock;
  }

  @Override
  public void addInstrument(final InstrumentDerivative instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Calibration instruments should be swaptions");
    final SwaptionPhysicalFixedIbor swaption = (SwaptionPhysicalFixedIbor) instrument;
    getBasket().add(instrument);
    getMethod().add(calculator);
    getCalibrationPrices().add(0.0);
    _instrumentIndex.add(Arrays.binarySearch(((SuccessiveLeastSquareLMMDDCalibrationObjective) _calibrationObjective).getLMMParameters().getIborTime(), swaption.getUnderlyingSwap().getSecondLeg()
        .getNthPayment(swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments() - 1).getPaymentTime()));
  }

  @Override
  public void calibrate(final DATA_TYPE data) {
    final int nbInstruments = getBasket().size();
    ArgumentChecker.isTrue(nbInstruments % _nbInstrumentsBlock == 0, "Number of instruments incompatible with block size");
    final int nbBlocks = nbInstruments / _nbInstrumentsBlock;
    computeCalibrationPrice(data);
    _calibrationObjective.setMulticurves(data.getMulticurveProvider());
    final SuccessiveLeastSquareLMMDDCalibrationObjective objective = (SuccessiveLeastSquareLMMDDCalibrationObjective) _calibrationObjective;
    final NonLinearLeastSquare ls = new NonLinearLeastSquare(DecompositionFactory.SV_COMMONS, MatrixAlgebraFactory.OG_ALGEBRA, DEFAULT_PRECISION);
    //    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[] instruments = new InstrumentDerivative[_nbInstrumentsBlock];
      final double[] prices = new double[_nbInstrumentsBlock];
      for (int loopins = 0; loopins < _nbInstrumentsBlock; loopins++) {
        instruments[loopins] = getBasket().get(loopblock * _nbInstrumentsBlock + loopins);
        prices[loopins] = getCalibrationPrices().get(loopblock * _nbInstrumentsBlock + loopins);
      }
      _calibrationObjective.setInstruments(instruments);
      _calibrationObjective.setPrice(prices);
      objective.setStartIndex(_instrumentIndex.get(loopblock * _nbInstrumentsBlock));
      objective.setEndIndex(_instrumentIndex.get((loopblock + 1) * _nbInstrumentsBlock) - 1);
      // Implementation note: the index start is from the first instrument of the block and the index end is from the last instrument of the block.
      final DoubleMatrix1D observedValues = new DoubleMatrix1D(_nbInstrumentsBlock, 0.0);
      @SuppressWarnings("unused")
      final
      LeastSquareResults result = ls.solve(observedValues, _calibrationObjective, new DoubleMatrix1D(1.0, 0.0));
      // Implementation note: the start value is a multiplicative factor of one and an additive term of 0 (parameters unchanged).
      //   The observed values are 0 as the function returns the difference between the calculated prices and the targets.
    }
  }

}
