/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.FraCashFlowDetailsCalculator;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculator initialised with the data required to perform
 * analytics calculations for a particular security.
 */
public interface FRACalculator {

  /**
   * Calculates the present value for the security
   *
   * @return result containing the PV if successfully created, a failure result otherwise
   */
  Result<MultipleCurrencyAmount> calculatePV();

  /**
   * Calculates the par rate for the security
   *
   * @return result containing the rate if successfully created, a failure result otherwise
   */
  Result<Double> calculateRate();

  /**
   * Calculates the PV for the security from the given curve
   *
   * @param bundle the curve bundle to price with
   * @return result containing the PV if successfully created, a failure result otherwise
   */
  Result<MultipleCurrencyAmount> calculatePv(MulticurveProviderInterface bundle);

  /**
   * Calculates the PV01 for the security
   *
   * @return result containing the PV01 if successfully created, a failure result otherwise
   */
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01();

  /**
   * Calculates the bucketed PV01 for the security
   *
   * @return the bucketed PV01
   */
  Result<BucketedCurveSensitivities> calculateBucketedPV01();

  /**
   * Calculates the bucketed Gamma (full matrix)
   *
   * @return the bucketed Gamma
   */
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma();

  /**
   * Calculates the bucketed Gamma without cross-gamma values
   *
   * @return the bucketed Gamma
   */
  Result<BucketedCurveSensitivities> calculateBucketedGamma();

  /**
   * Calculates receive cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @return the receive cashflows
   */
  Result<SwapLegCashFlows> calculateReceiveCashFlows();

  /**
   * Calculates pay cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @return the pay cashflows
   */
  Result<SwapLegCashFlows> calculatePayCashFlows();

}
