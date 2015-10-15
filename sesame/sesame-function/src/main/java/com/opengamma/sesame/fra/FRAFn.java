/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.FraCashFlowDetailsCalculator;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.ForwardRateAgreementTrade;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculate analytics values for a FRA.
 */
public interface FRAFn {

  /**
   * Calculate the par rate for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, FRASecurity security);

  /**
   * Calculate the present value for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, FRASecurity security);
  
  /**
   * Calculate the par rate for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the present value for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the PV01 for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the fra to calculate the PV01 for
   * @return result containing the PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the PV01 for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV01 for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, FRASecurity security);

  /**
   * Calculate the bucketed PV01 for a security.
   *
   * @param env the environment used for calculation
   * @param security the security to calculate the bucketed PV01 for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the bucketed PV01 for a security.
   *
   * @param env the environment used for calculation
   * @param security the security to calculate the bucketed PV01 for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, FRASecurity security);

  /**
   * Calculate the bucketed Gamma for a security.
   *
   * @param env the environment used for calculation
   * @param security the security to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma, full matrix, if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_CROSS_GAMMA)
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the bucketed Gamma for a security.
   *
   * @param env the environment used for calculation
   * @param security the security to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma, full matrix, if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_CROSS_GAMMA)
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, FRASecurity security);

  /* Trade based model integration */
  /**
   * Calculate the par rate for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(value = OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculate the present value for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(value = OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculate the PV01 for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the PV01 for
   * @return result containing the PV01 if successful, a Failure otherwise
   */
  @Output(value = OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculate the bucketed PV01 for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the bucketed PV01 for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculate the bucketed Gamma (full matrix) for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_CROSS_GAMMA)
  Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, ForwardRateAgreementTrade trade);
 
  /**
   * Calculate the bucketed Gamma project on curve pillars without cross values for a trade on a forward rate agreement.
   *
   * @param env the environment used for calculation
   * @param trade the trade on a forward rate agreement to calculate the bucketed Gamma for
   * @return result containing the bucketed Gamma if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_GAMMA)
  Result<BucketedCurveSensitivities> calculateBucketedGamma(Environment env, ForwardRateAgreementTrade trade);
  
  /**
   * Calculates receive cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @param env the environment used for calculation
   * @param trade the FRA to calculate cashflows for
   * @return the receive cashflows
   */
  @Output(OutputNames.RECEIVE_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculates pay cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @param env the environment used for calculation
   * @param trade the FRA to calculate cashflows for
   * @return the pay cashflows
   */
  @Output(OutputNames.PAY_LEG_CASH_FLOWS)
  Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, ForwardRateAgreementTrade trade);

  /**
   * Calculates pay cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @param env the environment used for calculation
   * @param security the security to price
   * @return the pay cashflows
   */
  Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculates receive cashflows on the FRA. See note on 
   * {@link FraCashFlowDetailsCalculator} for further details
   * on how these are generated.
   * 
   * @param env the environment used for calculation
   * @param security the security to price
   * @return the receive cashflows
   */
  Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, ForwardRateAgreementSecurity security);


  
}
