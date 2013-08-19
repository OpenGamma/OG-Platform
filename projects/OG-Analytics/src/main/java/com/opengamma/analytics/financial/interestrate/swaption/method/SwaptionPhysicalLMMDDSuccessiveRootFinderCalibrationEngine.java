/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Specific calibration engine for the LMM model with swaption.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine extends SuccessiveRootFinderCalibrationEngine {

  /**
   * The list of the last index in the Ibor date for each instrument.
   */
  private final List<Integer> _instrumentIndex = new ArrayList<>();

  /**
   * Constructor of the calibration engine.
   * @param calibrationObjective The calibration objective.
   */
  public SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective calibrationObjective) {
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
    ArgumentChecker.notNull(instrument, "Instrument");
    ArgumentChecker.notNull(method, "Method");
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Calibration instruments should be swaptions");
    final SwaptionPhysicalFixedIbor swaption = (SwaptionPhysicalFixedIbor) instrument;
    getBasket().add(instrument);
    getMethod().add(method);
    getCalibrationPrice().add(0.0);
    _instrumentIndex.add(Arrays.binarySearch(((SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective) getCalibrationObjective()).getLMMParameters().getIborTime(),
        swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments() - 1).getPaymentTime()));
  }

  @Override
  public void calibrate(final YieldCurveBundle curves) {
    computeCalibrationPrice(curves);
    getCalibrationObjective().setCurves(curves);
    final int nbInstruments = getBasket().size();
    final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective objective = (SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective) getCalibrationObjective();
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(objective.getFunctionValueAccuracy(), objective.getVariableAbsoluteAccuracy());
    final BracketRoot bracketer = new BracketRoot();
    for (int loopins = 0; loopins < nbInstruments; loopins++) {
      final InstrumentDerivative instrument = getBasket().get(loopins);
      getCalibrationObjective().setInstrument(instrument);
      objective.setStartIndex(_instrumentIndex.get(loopins));
      objective.setEndIndex(_instrumentIndex.get(loopins + 1) - 1);
      getCalibrationObjective().setPrice(getCalibrationPrice().get(loopins));
      final double[] range = bracketer.getBracketedPoints(getCalibrationObjective(), objective.getMinimumParameter(), objective.getMaximumParameter());
      rootFinder.getRoot(getCalibrationObjective(), range[0], range[1]);
    }
  }
}
