/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;

/**
 * Specific calibration engine for the Hull-White one factor model with swaption.
 */
public class SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentIndex = new ArrayList<Integer>();

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(SwaptionPhysicalLMMDDCalibrationObjective calibrationObjective) {
    super(calibrationObjective);
    _instrumentIndex.add(0);
  }

  /**
   * Gets the instrument index.
   * @return The instrument index.
   */
  public List<Integer> getInstrumentIndex() {
    return _instrumentIndex;
  }

  @Override
  public void addInstrument(final InstrumentDerivative instrument, final PricingMethod method) {
    Validate.notNull(instrument, "Instrument");
    Validate.notNull(method, "Method");
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Calibration instruments should be swaptions");
    SwaptionPhysicalFixedIbor swaption = (SwaptionPhysicalFixedIbor) instrument;
    getBasket().add(instrument);
    getMethod().add(method);
    getCalibrationPrice().add(0.0);
    _instrumentIndex.add(Arrays.binarySearch(((SwaptionPhysicalLMMDDCalibrationObjective) getCalibrationObjective()).getLMMParameters().getIborTime(), swaption.getUnderlyingSwap().getSecondLeg()
        .getNthPayment(swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments() - 1).getPaymentTime()));
  }

  @Override
  public void calibrate(YieldCurveBundle curves) {
    computeCalibrationPrice(curves);
    getCalibrationObjective().setCurves(curves);
    int nbInstruments = getBasket().size();
    SwaptionPhysicalLMMDDCalibrationObjective objective = (SwaptionPhysicalLMMDDCalibrationObjective) getCalibrationObjective();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(objective.getFunctionValueAccuracy(), objective.getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      InstrumentDerivative instrument = getBasket().get(loopins);
      getCalibrationObjective().setInstrument(instrument);
      objective.setStartIndex(_instrumentIndex.get(loopins));
      objective.setEndIndex(_instrumentIndex.get(loopins + 1) - 1);
      getCalibrationObjective().setPrice(getCalibrationPrice().get(loopins));
      final double[] range = bracketer.getBracketedPoints(getCalibrationObjective(), objective.getMinimumParameter(), objective.getMaximumParameter());
      rootFinder.getRoot(getCalibrationObjective(), range[0], range[1]);
    }
  }
}
