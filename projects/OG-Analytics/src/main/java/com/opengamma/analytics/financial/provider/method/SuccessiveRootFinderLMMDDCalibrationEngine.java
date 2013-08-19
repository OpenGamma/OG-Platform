/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Specific calibration engine for the Hull-White one factor model with cap/floor.
 * @param <DATA_TYPE>  The type of the data for the base calculator.
 */
public class SuccessiveRootFinderLMMDDCalibrationEngine<DATA_TYPE extends ParameterProviderInterface> extends CalibrationEngineWithCalculators<DATA_TYPE> {

  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentIndex = new ArrayList<>();

  /**
   * The calibration objective.
   */
  private final SuccessiveRootFinderCalibrationObjectiveWithMultiCurves _calibrationObjective;

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SuccessiveRootFinderLMMDDCalibrationEngine(final SuccessiveRootFinderCalibrationObjectiveWithMultiCurves calibrationObjective) {
    super(calibrationObjective.getFXMatrix(), calibrationObjective.getCcy());
    _calibrationObjective = calibrationObjective;
    _instrumentIndex.add(0);
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calculator The calculator.
   */
  @Override
  public void addInstrument(final InstrumentDerivative instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    ArgumentChecker.isTrue((instrument instanceof SwaptionPhysicalFixedIbor), "Instrument should be cap or swaption.");
    getBasket().add(instrument);
    getMethod().add(calculator);
    getCalibrationPrices().add(0.0);
    if (instrument instanceof SwaptionPhysicalFixedIbor) {
      final SwaptionPhysicalFixedIbor swaption = (SwaptionPhysicalFixedIbor) instrument;
      _instrumentIndex.add(Arrays.binarySearch(((SuccessiveRootFinderLMMDDCalibrationObjective) _calibrationObjective).getLMMParameters().getIborTime(), swaption.getUnderlyingSwap()
          .getSecondLeg().getNthPayment(swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments() - 1).getPaymentTime()));
    }
  }

  /**
   * Gets the instrument index.
   * @return The instrument index.
   */
  public List<Integer> getInstrumentIndex() {
    return _instrumentIndex;
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param calculator The calculator.
   */
  @Override
  public void addInstrument(final InstrumentDerivative[] instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    for (final InstrumentDerivative element : instrument) {
      addInstrument(element, calculator);
    }
  }

  @Override
  public void calibrate(final DATA_TYPE data) {
    computeCalibrationPrice(data);
    _calibrationObjective.setMulticurves(data.getMulticurveProvider());
    final int nbInstruments = getBasket().size();
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = (SuccessiveRootFinderLMMDDCalibrationObjective) _calibrationObjective;
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(_calibrationObjective.getFunctionValueAccuracy(), _calibrationObjective.getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      final InstrumentDerivative instrument = getBasket().get(loopins);
      _calibrationObjective.setInstrument(instrument);
      objective.setStartIndex(_instrumentIndex.get(loopins));
      objective.setEndIndex(_instrumentIndex.get(loopins + 1) - 1);
      _calibrationObjective.setPrice(getCalibrationPrices().get(loopins));
      final double[] range = bracketer.getBracketedPoints(_calibrationObjective, objective.getMinimumParameter(), objective.getMaximumParameter());
      rootFinder.getRoot(_calibrationObjective, range[0], range[1]);
    }
  }

}
