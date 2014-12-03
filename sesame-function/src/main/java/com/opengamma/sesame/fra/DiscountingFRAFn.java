/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.trade.ForwardRateAgreementTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;


/**
 * Calculate discounting PV and par rate for a FRA.
 */
public class DiscountingFRAFn implements FRAFn {

  private final FRACalculatorFactory _fraCalculatorFactory;

  /**
   * Create the function.
   *
   * @param fraCalculatorFactory function to generate the calculator for the security
   */
  public DiscountingFRAFn(FRACalculatorFactory fraCalculatorFactory) {
    _fraCalculatorFactory = fraCalculatorFactory;
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, FRASecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV();
  }

  @Override
  public Result<Double> calculateParRate(Environment env, FRASecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateRate();
  }
  
  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, ForwardRateAgreementSecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV();
  }

  @Override
  public Result<Double> calculateParRate(Environment env, ForwardRateAgreementSecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateRate();
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, ForwardRateAgreementSecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV01();
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, FRASecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV01();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, FRASecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedPV01();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env,
                                                                  ForwardRateAgreementSecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);

    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedPV01();
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedGamma(Environment env, FRASecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedGamma();
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedGamma(Environment env,
                                                                   ForwardRateAgreementSecurity security) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, security);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedGamma();
  }

  @Override
  public Result<Double> calculateParRate(Environment env, ForwardRateAgreementTrade trade) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, trade);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateRate();
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, ForwardRateAgreementTrade trade) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, trade);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV();
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, ForwardRateAgreementTrade trade) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, trade);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculatePV01();
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, ForwardRateAgreementTrade trade) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, trade);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedPV01();
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedGamma(Environment env, ForwardRateAgreementTrade trade) {
    Result<FRACalculator> calculatorResult = _fraCalculatorFactory.createCalculator(env, trade);
    if (!calculatorResult.isSuccess()) {
      return Result.failure(calculatorResult);
    }
    return calculatorResult.getValue().calculateBucketedGamma();
  }

}
