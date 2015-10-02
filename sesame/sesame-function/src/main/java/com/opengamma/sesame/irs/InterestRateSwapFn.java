/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculate analytics values for a Swap.
 */
public interface InterestRateSwapFn {
  
  /* Security based model integration */

  /**
   * Calculate the par rate for a Swap security.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, InterestRateSwapSecurity security);

  /**
   * Compute the spread to be added to the market standard quote of the instrument for
   * which the present value of the instrument is zero.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_SPREAD)
  Result<Double> calculateParSpread(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the present value for a Swap security.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the PV01 for a Swap security.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the PV01 for
   * @return result containing the PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the receive leg full cash flow for a Swap leg, including past cash flows.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.FULL_RECEIVE_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculateFullReceiveLegCashFlows(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the pay leg full cash flow for a Swap leg, including past cash flows.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.FULL_PAY_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculateFullPayLegCashFlows(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the receive leg cash flow for a Swap leg.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.RECEIVE_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the pay leg cash flow for a Swap leg.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.PAY_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the receive leg present value.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the present value for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.RECEIVE_LEG_PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculateReceiveLegPv(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the pay leg cash present value.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the present value for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.PAY_LEG_PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePayLegPv(Environment env, InterestRateSwapSecurity security);

  /**
   * Calculate the bucketed PV01 for a swap security.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the bucketed PV01 for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, InterestRateSwapSecurity security);
  
  /**
   * Calculate the bucketed Gamma for a swap security.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapSecurity to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma, full matrix, if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_CROSS_GAMMA)
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, InterestRateSwapSecurity security);
  
  /* Trade based model integration */

  /**
   * Calculate the par rate for a Swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the present value for a Swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the PV01 for a Swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the PV01 for
   * @return result containing the PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the receive leg cash flow for a Swap leg.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.RECEIVE_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the pay leg cash flow for a Swap leg.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the cash flows for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.PAY_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the receive leg present value.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapTrade to calculate the present value for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.RECEIVE_LEG_PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculateReceiveLegPv(Environment env, InterestRateSwapTrade security);

  /**
   * Calculate the pay leg cash present value.
   *
   * @param env the environment used for calculation
   * @param security the InterestRateSwapTrade to calculate the present value for
   * @return result containing the fixed cash flows if successful, a Failure otherwise
   */
  @Output(OutputNames.PAY_LEG_PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePayLegPv(Environment env, InterestRateSwapTrade security);

  /**
   * Calculate the bucketed PV01 for a swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the bucketed PV01 for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, InterestRateSwapTrade trade);

  /**
   * Calculate the bucketed Gamma (full matrix) for a swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_CROSS_GAMMA)
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, InterestRateSwapTrade trade);
 
  /**
   * Calculate the bucketed Gamma project on curve pillars without cross values for a swap trade.
   *
   * @param env the environment used for calculation
   * @param trade the InterestRateSwapTrade to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_GAMMA)
  Result<BucketedCurveSensitivities> calculateBucketedGamma(Environment env, InterestRateSwapTrade trade);

}
