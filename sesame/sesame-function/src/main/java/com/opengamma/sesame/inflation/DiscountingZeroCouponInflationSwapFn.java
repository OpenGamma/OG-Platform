/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParRateInflationDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.InflationProviderBundle;
import com.opengamma.sesame.LookupInflationProviderFn;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Discounting function for calculating risk measures for zero coupon inflation swaps.
 */
public class DiscountingZeroCouponInflationSwapFn implements ZeroCouponInflationSwapFn {

  private final InflationSwapConverterFn _converter;
  private final LookupInflationProviderFn _inflationProviderFn;
  private final CurveLabellingFn _curveLabellingFn;

  private static final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, MultipleCurrencyAmount> PV_CALC =
      PresentValueDiscountingInflationCalculator.getInstance();

  private static final ParRateInflationDiscountingCalculator PAR_RATE_CALC =
      ParRateInflationDiscountingCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDIC =
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC =
      new ParameterSensitivityInflationParameterCalculator<>(PVCSDIC);
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationProviderInterface> MQSBC =
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSC);
  private static final double BP1 = 1.0E-4;

  public DiscountingZeroCouponInflationSwapFn(InflationSwapConverterFn converter,
                                              LookupInflationProviderFn inflationProviderFn,
                                              CurveLabellingFn curveLabellingFn) {
    _inflationProviderFn = ArgumentChecker.notNull(inflationProviderFn, "inflationProviderFn");
    _converter = ArgumentChecker.notNull(converter, "converter");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
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
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, ZeroCouponInflationSwapTrade trade) {
    Result<InflationProviderBundle> bundleResult = _inflationProviderFn.getInflationBundle(env, trade);
    Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> tradeResults =
        _converter.convert(env, trade.getSecurity());

    if (!bundleResult.isSuccess() || !tradeResults.isSuccess()) {
      return Result.failure(bundleResult, tradeResults);
    } else {

      ParameterInflationProviderInterface data = bundleResult.getValue().getParameterInflationProvider();
      CurveBuildingBlockBundle block = bundleResult.getValue().getCurveBuildingBlockBundle();
      Set<String> curveNames = block.getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabels = _curveLabellingFn.getCurveLabellers(curveNames);

      if (!curveLabels.isSuccess()) {
        return Result.failure(curveLabels);
      }

      InstrumentDerivative swap = tradeResults.getValue().getSecond();
      MultipleCurrencyParameterSensitivity sensitivity = MQSBC.fromInstrument(swap, data, block).multipliedBy(BP1);
      Map<Pair<String, Currency>, DoubleLabelledMatrix1D> labelledMatrix1DMap = new HashMap<>();

      for (Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivity.getSensitivities().entrySet()) {
        CurveMatrixLabeller curveMatrixLabeller = curveLabels.getValue().get(entry.getKey().getFirst());
        DoubleLabelledMatrix1D matrix = curveMatrixLabeller.labelMatrix(entry.getValue());
        labelledMatrix1DMap.put(entry.getKey(), matrix);
      }

      return Result.success(BucketedCurveSensitivities.of(labelledMatrix1DMap));
    }
  }

  @Override
  public Result<Double> calculateParRate(Environment env, ZeroCouponInflationSwapTrade trade) {
    Result<InflationProviderBundle> bundleResult = _inflationProviderFn.getInflationBundle(env, trade);
    Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> tradeResults =
        _converter.convert(env, trade.getSecurity());

    if (!bundleResult.isSuccess() || !tradeResults.isSuccess()) {
      return Result.failure(bundleResult, tradeResults);
    } else {
      ParameterInflationProviderInterface data = bundleResult.getValue().getParameterInflationProvider();
      InstrumentDerivative inflationDerivative = tradeResults.getValue().getSecond();
      return Result.success(inflationDerivative.accept(PAR_RATE_CALC, data));
    }
  }

}
