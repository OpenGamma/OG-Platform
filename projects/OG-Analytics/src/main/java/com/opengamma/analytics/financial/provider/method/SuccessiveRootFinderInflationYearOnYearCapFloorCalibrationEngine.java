/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Specific calibration engine for the price index (inflation) market model with year on year cap/floor.
 * @param <DATA_TYPE>  The type of the data for the base calculator.
 */
public class SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<DATA_TYPE extends InflationProviderInterface> extends CalibrationEngineWithCalculators<DATA_TYPE> {

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
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calculator The calculator.
   */
  @Override
  public void addInstrument(final InstrumentDerivative instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    ArgumentChecker.isTrue((instrument instanceof CapFloorInflationYearOnYearInterpolation) || (instrument instanceof CapFloorInflationYearOnYearMonthly),
        "Instrument should be cap inflation year on year.");
    getBasket().add(instrument);
    getMethod().add(calculator);
    getCalibrationPrice().add(0.0);
    if (instrument instanceof CapFloorIbor) {
      _calibrationTimes.add(((CapFloorIbor) instrument).getFixingTime());
    }
    if (instrument instanceof SwaptionPhysicalFixedIbor) {
      _calibrationTimes.add(((SwaptionPhysicalFixedIbor) instrument).getTimeToExpiry());
    }

  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param calculator The calculator.
   */
  @Override
  public void addInstrument(final InstrumentDerivative[] instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    for (int loopinstrument = 0; loopinstrument < instrument.length; loopinstrument++) {
      Validate.isTrue((instrument[loopinstrument] instanceof CapFloorInflationYearOnYearInterpolation) || (instrument[loopinstrument] instanceof CapFloorInflationYearOnYearMonthly),
          "Calibration instruments should be cap/floor inflation year on year");
      getBasket().add(instrument[loopinstrument]);
      getMethod().add(calculator);
      getCalibrationPrice().add(0.0);
      _calibrationTimes.add(((CapFloorIbor) instrument[loopinstrument]).getFixingTime());
    }
  }

  @Override
  public void calibrate(DATA_TYPE data) {
    computeCalibrationPrice(data);
    _calibrationObjective.setInflation(data.getInflationProvider());
    int nbInstruments = getBasket().size();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(_calibrationObjective.getFunctionValueAccuracy(), _calibrationObjective.getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      InstrumentDerivative instrument = getBasket().get(loopins);
      _calibrationObjective.setInstrument(instrument);
      _calibrationObjective.setPrice(getCalibrationPrice().get(loopins));
      final double[] range = bracketer.getBracketedPoints(_calibrationObjective, _calibrationObjective.getMinimumParameter(), _calibrationObjective.getMaximumParameter());
      rootFinder.getRoot(_calibrationObjective, range[0], range[1]);
      if (loopins < nbInstruments - 1) {
        ((SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective) _calibrationObjective).setNextCalibrationTime(_calibrationTimes.get(loopins));
      }
    }
  }

}
