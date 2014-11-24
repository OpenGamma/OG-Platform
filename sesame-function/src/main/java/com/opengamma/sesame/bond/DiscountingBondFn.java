/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bond;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ZSpreadIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.cache.CacheKey;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Discounting function for calculating risk measures for bonds.
 */
public class DiscountingBondFn implements BondFn {

  private static final double BASIS_POINT_FACTOR = 1.0E-4;

  private final BondAndBondFutureTradeConverter _converter;
  private final IssuerProviderFn _issuerProviderFn;
  private final MarketDataFn _marketDataFn;
  private final FunctionCache _cache;
  private final CurveLabellingFn _curveLabellingFn;

  private final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> _bucketedPv01Calculator =
      new MarketQuoteSensitivityBlockCalculator<>(
          new ParameterSensitivityParameterCalculator<>(
              PresentValueCurveSensitivityIssuerCalculator.getInstance()));

  private final PV01CurveParametersCalculator<ParameterIssuerProviderInterface> _pv01Calculator =
      new PV01CurveParametersCalculator<>(
          PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * @param converter converts bond trades to analytics objects
   * @param issuerProviderFn provides issuer curves
   * @param marketDataFn provides market data
   * @param curveLabellingFn provides labels for curve tenors used in building results objects
   * @param cache for caching the derivatives objects so they don't have to be rebuilt for every calculation
   */
  public DiscountingBondFn(BondAndBondFutureTradeConverter converter,
                           IssuerProviderFn issuerProviderFn,
                           MarketDataFn marketDataFn,
                           CurveLabellingFn curveLabellingFn,
                           FunctionCache cache) {
    _curveLabellingFn = curveLabellingFn;
    _marketDataFn = ArgumentChecker.notNull(marketDataFn, "marketDataFn");
    _issuerProviderFn = ArgumentChecker.notNull(issuerProviderFn, "issuerProviderFn");
    _converter = ArgumentChecker.notNull(converter, "converter");
    _cache = ArgumentChecker.notNull(cache, "cache");
  }

  // TODO convert to Result.flatMap() after Java 8

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromCurves(Environment env, BondTrade trade) {
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (!bundleResult.isSuccess()) {
      return Result.failure(bundleResult);
    }
    IssuerProviderBundle bundle = bundleResult.getValue();
    PresentValueIssuerCalculator calculator = PresentValueIssuerCalculator.getInstance();
    return Result.success(derivative(env, trade).accept(calculator, bundle.getParameterIssuerProvider()));

  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromCleanPrice(Environment env, BondTrade trade) {
    Result<Double> marketResult = calculateCleanPriceMarket(env, trade);
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (Result.anyFailures(marketResult, bundleResult)) {
      return Result.failure(marketResult, bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    BondTransactionDiscountingMethod calculator = BondTransactionDiscountingMethod.getInstance();
    return Result.success(calculator.presentValueFromCleanPrice(derivative(env, trade),
                                                                curves.getIssuerProvider(),
                                                                marketResult.getValue()));
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromYield(Environment env, BondTrade trade) {
    Result<Double> marketResult = calculateYieldToMaturityMarket(env, trade);
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (Result.anyFailures(marketResult, bundleResult)) {
      return Result.failure(marketResult, bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    BondTransactionDiscountingMethod calculator = BondTransactionDiscountingMethod.getInstance();
    return Result.success(calculator.presentValueFromYield(derivative(env, trade),
                                                           curves.getIssuerProvider(),
                                                           marketResult.getValue()));
  }

  @Override
  public Result<Double> calculateCleanPriceMarket(Environment env, BondTrade trade) {
    return _marketDataFn.getMarketValue(env, trade.getSecurity().getExternalIdBundle());
  }

  @Override
  public Result<Double> calculateCleanPriceFromCurves(Environment env, BondTrade trade) {
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (!bundleResult.isSuccess()) {
      return Result.failure(bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    BondSecurityDiscountingMethod calculator = BondSecurityDiscountingMethod.getInstance();
    return Result.success(calculator.cleanPriceFromCurves(derivative(env, trade).getBondStandard(),
                                                          curves.getIssuerProvider()));
  }

  @Override
  public Result<Double> calculateCleanPriceFromYield(Environment env, BondTrade trade) {
    Result<Double> marketResult = calculateYieldToMaturityMarket(env, trade);

    if (!marketResult.isSuccess()) {
      return Result.failure(marketResult);
    }
    BondSecurityDiscountingMethod calculator = BondSecurityDiscountingMethod.getInstance();
    return Result.success(calculator.cleanPriceFromYield(derivative(env, trade).getBondStandard(),
                                                         marketResult.getValue()));

  }

  @Override
  public Result<Double> calculateYieldToMaturityFromCleanPrice(Environment env, BondTrade trade) {
    Result<Double> marketResult = calculateCleanPriceMarket(env, trade);

    if (!marketResult.isSuccess()) {
      return Result.failure(marketResult);
    }
    BondSecurityDiscountingMethod calculator = BondSecurityDiscountingMethod.getInstance();
    return Result.success(calculator.yieldFromCleanPrice(derivative(env, trade).getBondStandard(),
                                                         marketResult.getValue()));
  }

  @Override
  public Result<Double> calculateYieldToMaturityFromCurves(Environment env, BondTrade trade) {
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (!bundleResult.isSuccess()) {
      return Result.failure(bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    BondSecurityDiscountingMethod calculator = BondSecurityDiscountingMethod.getInstance();
    return Result.success(calculator.yieldFromCurves(derivative(env, trade).getBondStandard(),
                                                     curves.getIssuerProvider()));
  }

  @Override
  public Result<Double> calculateYieldToMaturityMarket(Environment env, BondTrade trade) {
    FieldName fieldName = FieldName.of(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID);
    Result<?> result = _marketDataFn.getValue(env, trade.getSecurity().getExternalIdBundle(), fieldName);

    if (!result.isSuccess()) {
      return Result.failure(result);
    } else {
      return Result.success((Double) result.getValue());
    }
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, BondTrade trade) {
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (!bundleResult.isSuccess()) {
      return Result.failure(bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    CurveBuildingBlockBundle blocks = bundleResult.getValue().getCurveBuildingBlockBundle();
    Set<String> curveNames = bundleResult.getValue().getCurveBuildingBlockBundle().getData().keySet();
    Result<Map<String, CurveMatrixLabeller>> curveLabellersResult = _curveLabellingFn.getCurveLabellers(curveNames);

    if (!curveLabellersResult.isSuccess()) {
      return Result.failure(curveLabellersResult);
    }
    Map<String, CurveMatrixLabeller> curveLabellers = curveLabellersResult.getValue();
    MultipleCurrencyParameterSensitivity sensitivity =
        _bucketedPv01Calculator.fromInstrument(derivative(env, trade), curves, blocks).multipliedBy(BASIS_POINT_FACTOR);

    Map<Pair<String, Currency>, DoubleLabelledMatrix1D> labelledMatrix1DMap = new HashMap<>();

    for (Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivity.getSensitivities().entrySet()) {
      CurveMatrixLabeller labeller = curveLabellers.get(entry.getKey().getFirst());
      DoubleLabelledMatrix1D matrix = labeller.labelMatrix(entry.getValue());
      labelledMatrix1DMap.put(entry.getKey(), matrix);
    }
    return Result.success(BucketedCurveSensitivities.of(labelledMatrix1DMap));
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, BondTrade trade) {
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (!bundleResult.isSuccess()) {
      return Result.failure(bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    return Result.success(derivative(env, trade).accept(_pv01Calculator, curves));
  }

  @Override
  public Result<Double> calculateZSpread(Environment env, BondTrade trade) {
    Result<Double> marketResult = calculateCleanPriceMarket(env, trade);
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, trade.getTrade());

    if (Result.anyFailures(marketResult, bundleResult)) {
      return Result.failure(marketResult, bundleResult);
    }
    ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
    Pair<IssuerProviderInterface, Double> pair = Pairs.of(curves.getIssuerProvider(), marketResult.getValue());
    return Result.success(derivative(env, trade).accept(ZSpreadIssuerCalculator.getInstance(), pair));
  }

  private BondFixedTransactionDefinition definition(final BondTrade trade) {
    return _cache.get(CacheKey.of(this, trade), new Callable<BondFixedTransactionDefinition>() {
      @Override
      public BondFixedTransactionDefinition call() throws Exception {
        return (BondFixedTransactionDefinition) _converter.convert(trade.getTrade());
      }
    });
  }

  private BondFixedTransaction derivative(Environment env, BondTrade trade) {
    final BondFixedTransactionDefinition definition = definition(trade);
    final ZonedDateTime valuationTime = env.getValuationTime();

    return _cache.get(CacheKey.of(this, valuationTime, trade), new Callable<BondFixedTransaction>() {
      @Override
      public BondFixedTransaction call() throws Exception {
        return definition.toDerivative(valuationTime);
      }
    });
  }
}
