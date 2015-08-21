/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.InflationProviderBundle;
import com.opengamma.sesame.LookupInflationProviderFn;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Discounting function for calculating risk measures for zero coupon inflation swaps.
 */
public class DiscountingZeroCouponInflationSwapFn implements ZeroCouponInflationSwapFn {

  private final InflationSwapConverterFn _converter;
  private final LookupInflationProviderFn _inflationProviderFn;

  private static final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, MultipleCurrencyAmount> PV_CALC =
      PresentValueDiscountingInflationCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDIC =
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC =
      new ParameterSensitivityInflationParameterCalculator<>(PVCSDIC);
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationProviderInterface> MQSBC =
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSC);
  private static final double BP1 = 1.0E-4;

  public DiscountingZeroCouponInflationSwapFn(InflationSwapConverterFn converter,
                                              LookupInflationProviderFn inflationProviderFn) {
    _inflationProviderFn = ArgumentChecker.notNull(inflationProviderFn, "inflationProviderFn");
    _converter = ArgumentChecker.notNull(converter, "converter");
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, ZeroCouponInflationSwapTrade trade) {

    Result<InflationProviderBundle> bundleResult = _inflationProviderFn.getInflationBundle(env, trade);
    Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> tradeResults =
        _converter.convert(env, trade.getSecurity());

    if (!bundleResult.isSuccess() || !tradeResults.isSuccess()) {
      return Result.failure(bundleResult, tradeResults);
    } else {

      ParameterInflationProviderInterface data = bundleResult.getValue().getParameterInflationProvider();
      InstrumentDerivative inflationDerivative = tradeResults.getValue().getSecond();
      return Result.success(inflationDerivative.accept(PV_CALC, data));

    }

  }

  @Override
  public Result<MultipleCurrencyParameterSensitivity> calculateBucketedPV01(Environment env, ZeroCouponInflationSwapTrade trade) {
    Result<InflationProviderBundle> bundleResult = _inflationProviderFn.getInflationBundle(env, trade);
    Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> tradeResults =
        _converter.convert(env, trade.getSecurity());

    if (!bundleResult.isSuccess() || !tradeResults.isSuccess()) {
      return Result.failure(bundleResult, tradeResults);
    } else {

      ParameterInflationProviderInterface data = bundleResult.getValue().getParameterInflationProvider();
      CurveBuildingBlockBundle block = bundleResult.getValue().getCurveBuildingBlockBundle();
      InstrumentDerivative inflationDerivative = tradeResults.getValue().getSecond();
      return Result.success(MQSBC.fromInstrument(inflationDerivative, data, block).multipliedBy(BP1));

    }
  }


}
