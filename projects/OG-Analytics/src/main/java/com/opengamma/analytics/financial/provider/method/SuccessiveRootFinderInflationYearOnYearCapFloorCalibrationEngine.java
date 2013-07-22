/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Specific calibration engine for the price index (inflation) market model with year on year cap/floor.
 * @param <DATA_TYPE>  The type of the data for the base calculator.
 */
public class SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<DATA_TYPE extends InflationProviderInterface> extends CalibrationEngineWithPrices<DATA_TYPE> {

  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentExpiryIndex = new ArrayList<Integer>();

  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentStrikeIndex = new ArrayList<Integer>();

  /**
   * The list of calibration times.
   */
  private final List<Double> _calibrationTimes = new ArrayList<Double>();

  /**
   * The calibration objective.
   */
  private final SuccessiveRootFinderCalibrationObjectivewithInflation _calibrationObjective;

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine(SuccessiveRootFinderCalibrationObjectivewithInflation calibrationObjective) {
    super(calibrationObjective.getFXMatrix(), calibrationObjective.getCcy());
    _calibrationObjective = calibrationObjective;
    _instrumentExpiryIndex.add(0);
    _instrumentStrikeIndex.add(0);
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calibrationPrice The price of the instrument we want to calibrate on.
   */
  @Override
  public void addInstrument(final InstrumentDerivative instrument, final double calibrationPrice) {
    ArgumentChecker.isTrue((instrument instanceof CapFloorInflationYearOnYearInterpolation) || (instrument instanceof CapFloorInflationYearOnYearMonthly),
        "Instrument should be cap inflation year on year.");
    getBasket().add(instrument);
    getCalibrationPrices().add(calibrationPrice);
    if (instrument instanceof CapFloorInflationYearOnYearInterpolation) {
      CapFloorInflationYearOnYearInterpolation cap = (CapFloorInflationYearOnYearInterpolation) instrument;
      _calibrationTimes.add(cap.getPaymentTime());
      _instrumentExpiryIndex.add(Arrays.binarySearch(((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).getInflationCapYearOnYearParameters()
          .getExpiryTimes(), cap.getPaymentTime()));
      _instrumentStrikeIndex.add(Arrays.binarySearch(((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).getInflationCapYearOnYearParameters().getStrikes(),
          cap.getStrike()));
    }
    if (instrument instanceof CapFloorInflationYearOnYearMonthly) {
      CapFloorInflationYearOnYearMonthly cap = (CapFloorInflationYearOnYearMonthly) instrument;
      _calibrationTimes.add(cap.getPaymentTime());
      _instrumentExpiryIndex.add(Arrays.binarySearch(((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).getInflationCapYearOnYearParameters()
          .getExpiryTimes(), cap.getPaymentTime()));
      _instrumentStrikeIndex.add(Arrays.binarySearch(((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).getInflationCapYearOnYearParameters().getStrikes(),
          cap.getStrike()));
    }

  }

  /**
   * Gets the instrument index.
   * @return The instrument index.
   */
  public List<Integer> getInstrumentExpiryIndex() {
    return _instrumentExpiryIndex;
  }

  /**
   * Gets the instrument index.
   * @return The instrument index.
   */
  public List<Integer> getInstrumentStrikeIndex() {
    return _instrumentStrikeIndex;
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param calibrationPrices The prices of the instruments we want to calibrate on.
   */
  @Override
  public void addInstrument(final InstrumentDerivative[] instrument, final double[] calibrationPrices) {
    for (int loopinstrument = 0; loopinstrument < instrument.length; loopinstrument++) {
      addInstrument(instrument[loopinstrument], calibrationPrices[loopinstrument]);
    }
  }

  @Override
  public void calibrate(DATA_TYPE data) {
    _calibrationObjective.setInflation(data.getInflationProvider());
    int nbInstruments = getBasket().size();
    SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = (SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective;
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(_calibrationObjective.getFunctionValueAccuracy(), _calibrationObjective.getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      InstrumentDerivative instrument = getBasket().get(loopins);
      _calibrationObjective.setInstrument(instrument);
      _calibrationObjective.setPrice(getCalibrationPrices().get(loopins));
      objective.setExpiryIndex(_instrumentExpiryIndex.get(loopins+1));
      objective.setStrikeIndex(_instrumentStrikeIndex.get(loopins+1));
      final double[] range = bracketer.getBracketedPoints(_calibrationObjective, _calibrationObjective.getMinimumParameter(), _calibrationObjective.getMaximumParameter());
      rootFinder.getRoot(_calibrationObjective, range[0], range[1]);
      if (loopins < nbInstruments - 1) {
        ((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).setNextCalibrationTime(_calibrationTimes.get(loopins));
      }
    }
  }

}
