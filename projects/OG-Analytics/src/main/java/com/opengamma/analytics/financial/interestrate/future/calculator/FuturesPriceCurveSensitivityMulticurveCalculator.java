/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price curve sensitivity for different types of futures. Calculator using a multi-curve provider.
 */
public final class FuturesPriceCurveSensitivityMulticurveCalculator extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityMulticurveCalculator INSTANCE = new FuturesPriceCurveSensitivityMulticurveCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityMulticurveCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityMulticurveCalculator() {
  }

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurve, "Multi-curves provider");
    final double priceBar = 1.0;
    final double forwardBar = -priceBar;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getMulticurveProvider().getName(futures.getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

  @Override
  public MulticurveSensitivity visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurve, "Multi-curves provider");
    final IndexON index = futures.getIndex();
    final int nbFixing = futures.getFixingPeriodAccrualFactor().length;
    final double[] rates = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      rates[loopfix] = multicurve.getMulticurveProvider().getSimplyCompoundForwardRate(index, futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1],
          futures.getFixingPeriodAccrualFactor()[loopfix]);
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double interestBar = -1.0 / futures.getFixingTotalAccrualFactor() * priceBar;
    final double[] ratesBar = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      ratesBar[loopfix] = futures.getFixingPeriodAccrualFactor()[loopfix] * interestBar;
    }
    final Map<String, List<ForwardSensitivity>> resultMap = new HashMap<>();
    final List<ForwardSensitivity> listON = new ArrayList<>();
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      listON.add(new SimplyCompoundedForwardSensitivity(futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1], futures.getFixingPeriodAccrualFactor()[loopfix],
          ratesBar[loopfix]));
    }
    resultMap.put(multicurve.getMulticurveProvider().getName(index), listON);
    return MulticurveSensitivity.ofForward(resultMap);
  }

}
