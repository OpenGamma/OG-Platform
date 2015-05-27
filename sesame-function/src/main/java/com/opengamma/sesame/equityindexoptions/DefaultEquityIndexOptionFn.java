/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equityindexoptions;

import java.util.LinkedHashMap;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotDeltaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotGammaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.conversion.EquityOptionsConverter;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pairs;

/**
 * Default implementation of the {@link EquityIndexOptionFn}.
 */
public class DefaultEquityIndexOptionFn implements EquityIndexOptionFn {

  public static final double BP = 0.0001;
  private StaticReplicationDataBundleFn _dataProviderFn;
  private static final EquityOptionBlackPresentValueCalculator PV_CALC = EquityOptionBlackPresentValueCalculator.getInstance();

  private static final EquityOptionBlackSpotDeltaCalculator DELTA_CALC = EquityOptionBlackSpotDeltaCalculator.getInstance();
  private static final EquityOptionBlackSpotGammaCalculator GAMMA_CALC = EquityOptionBlackSpotGammaCalculator.getInstance();
  private static final EquityOptionBlackVegaCalculator VEGA_CALC = EquityOptionBlackVegaCalculator.getInstance();
  private static final EquityDerivativeSensitivityCalculator SENSITIVITY_CALC = new EquityDerivativeSensitivityCalculator(PV_CALC);

  /**
   * Constructs a function to calculate values for a equity index option.
   * @param dataProviderFn data provider bundle.
   */
  public DefaultEquityIndexOptionFn(StaticReplicationDataBundleFn dataProviderFn) {
    _dataProviderFn = ArgumentChecker.notNull(dataProviderFn, "dataProviderFn");
  }

  @Override
  public Result<CurrencyAmount> calculatePv(Environment env, EquityIndexOptionTrade trade) {
    Result<Double> result = calculateResult(env, trade, PV_CALC);
    if (result.isSuccess()) {
      CurrencyAmount amount = CurrencyAmount.of(trade.getSecurity().getCurrency(), result.getValue());
      return Result.success(amount);
    } else {
      return Result.failure(result);
    }
  }

  @Override
  public Result<Double> calculateDelta(Environment env, EquityIndexOptionTrade trade) {
    return calculateResult(env, trade, DELTA_CALC);
  }

  @Override
  public Result<Double> calculateGamma(Environment env, EquityIndexOptionTrade trade) {
    Result<StaticReplicationDataBundle> dataResult = _dataProviderFn.getEquityIndexDataProvider(env, trade);
    if (dataResult.isSuccess()) {
      // Gamma should be multiply by spot underlying
      double spot = dataResult.getValue().getForwardCurve().getSpot() / 100;
      Result<Double> result = calculateResult(env, trade, GAMMA_CALC);
      if (result.isSuccess()) {
        return Result.success(result.getValue() * spot);
      } else {
        return Result.failure(result);
      }
    } else {
      return Result.failure(dataResult);
    }
  }

  @Override
  public Result<Double> calculateVega(Environment env, EquityIndexOptionTrade trade) {
    Result<Double> result = calculateResult(env, trade, VEGA_CALC);
    if (result.isSuccess()) {
      // Vega should be divided 100
      return Result.success(result.getValue() / 100);
    } else {
      return Result.failure(result);
    }
  }

  @Override
  public Result<Double> calculatePv01(Environment env, EquityIndexOptionTrade trade) {
    Result<StaticReplicationDataBundle> dataResult = _dataProviderFn.getEquityIndexDataProvider(env, trade);
    if (Result.allSuccessful(dataResult)) {
      StaticReplicationDataBundle bundle = dataResult.getValue();
      InstrumentDerivative derivative = createInstrumentDerivative(trade, env.getValuationTime());
      return Result.success(SENSITIVITY_CALC.calcPV01(derivative, bundle));
    } else {
      return Result.failure(dataResult);
    }
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPv01(Environment env, EquityIndexOptionTrade trade) {
    Result<StaticReplicationDataBundle> dataResult = _dataProviderFn.getEquityIndexDataProvider(env, trade);
    if (Result.allSuccessful(dataResult)) {
      StaticReplicationDataBundle bundle = dataResult.getValue();
      InstrumentDerivative derivative = createInstrumentDerivative(trade, env.getValuationTime());
      DoubleMatrix1D deltaBucketed = SENSITIVITY_CALC.calcDeltaBucketed(derivative, bundle);
      YieldAndDiscountCurve discountCurve = bundle.getDiscountCurve();

      if (discountCurve instanceof YieldCurve) {
        Currency currency = trade.getSecurity().getCurrency();
        String name = discountCurve.getName();
        Double[] tenors = ((YieldCurve) discountCurve).getCurve().getXData();
        ImmutableList.Builder<String> dayBuilder = ImmutableList.builder();

        for (double tenor : tenors) {
          String day = Math.round(tenor * 365) + "D";
          dayBuilder.add(day);
        }

        LinkedHashMap<String, DoubleMatrix1D> map = new LinkedHashMap<>();
        map.put(name, deltaBucketed);
        SimpleParameterSensitivity sensitivity = new SimpleParameterSensitivity(map);
        CurveMatrixLabeller labeller = new CurveMatrixLabeller(dayBuilder.build());
        DoubleLabelledMatrix1D doubleLabelledMatrix1D =
            labeller.labelMatrix(sensitivity.multipliedBy(BP).getSensitivity(name));
        ImmutableMap.of(Pairs.of(name, currency),doubleLabelledMatrix1D);
        return Result.success(BucketedCurveSensitivities.of(ImmutableMap.of(Pairs.of(name, currency),
                                                                            doubleLabelledMatrix1D)));
      } else {
        return Result.failure(FailureStatus.INVALID_INPUT, "Can only handle YieldCurve instances of YieldAndDiscountCurve");
      }

    } else {
      return Result.failure(dataResult);
    }
  }

  private Result<Double> calculateResult(Environment env,
                                         EquityIndexOptionTrade trade,
                                         InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> calculator) {
    Result<StaticReplicationDataBundle> dataResult = _dataProviderFn.getEquityIndexDataProvider(env, trade);
    if (Result.allSuccessful(dataResult)) {
      StaticReplicationDataBundle bundle = dataResult.getValue();
      InstrumentDerivative derivative = createInstrumentDerivative(trade, env.getValuationTime());
      return Result.success(derivative.accept(calculator, bundle));
    } else {
      return Result.failure(dataResult);
    }

  }

  private InstrumentDerivative createInstrumentDerivative(EquityIndexOptionTrade tradeWrapper, ZonedDateTime valTime) {
    EquityOptionsConverter converter = new EquityOptionsConverter();
    EquityIndexOptionSecurity security = tradeWrapper.getSecurity();
    InstrumentDefinition<?> definition = security.accept(converter);
    return definition.toDerivative(valTime);
  }

}
