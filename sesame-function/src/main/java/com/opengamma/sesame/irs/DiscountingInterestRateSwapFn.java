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
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculate discounting PV and par rate for a Swap.
 */
public class DiscountingInterestRateSwapFn implements InterestRateSwapFn {

  private final InterestRateSwapCalculatorFactory _interestRateSwapCalculatorFactory;

  /**
   * Create the function.
   *
   * @param interestRateSwapCalculatorFactory function to generate the calculator for the security
   */
  public DiscountingInterestRateSwapFn(InterestRateSwapCalculatorFactory interestRateSwapCalculatorFactory) {
    _interestRateSwapCalculatorFactory = interestRateSwapCalculatorFactory;

  }

  /* Security based model integration */

  @Override
  public Result<Double> calculateParRate(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult =
        _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateRate();
  }

  @Override
  public Result<Double> calculateParSpread(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult =
        _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateParSpread();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV();
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV01();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedPV01();
  }

  @Override
  public Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateReceiveLegCashFlows();
  }

  @Override
  public Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePayLegCashFlows();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculateReceiveLegPv(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateReceiveLegPv();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePayLegPv(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePayLegPv();
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, InterestRateSwapSecurity security) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, security);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedCrossGamma();
  }
  
  /* Trade based model integration */
  @Override
  public Result<Double> calculateParRate(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateRate();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV();
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV01();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedPV01();
  }

  @Override
  public Result<SwapLegCashFlows> calculateReceiveLegCashFlows(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateReceiveLegCashFlows();
  }

  @Override
  public Result<SwapLegCashFlows> calculatePayLegCashFlows(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePayLegCashFlows();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculateReceiveLegPv(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateReceiveLegPv();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePayLegPv(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePayLegPv();
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedCrossGamma(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);
    
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedCrossGamma();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedGamma(Environment env, InterestRateSwapTrade trade) {
    Result<InterestRateSwapCalculator> calculatorResult = _interestRateSwapCalculatorFactory.createCalculator(env, trade);
    
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedGamma();
  }
}
