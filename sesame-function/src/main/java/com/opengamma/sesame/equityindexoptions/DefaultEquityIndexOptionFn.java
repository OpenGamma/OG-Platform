/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equityindexoptions;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotDeltaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotGammaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.analytics.conversion.EquityOptionsConverter;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Default implementation of the {@link EquityIndexOptionFn}.
 */
public class DefaultEquityIndexOptionFn implements EquityIndexOptionFn {

  private final StaticReplicationDataBundleFn _dataProviderFn;
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
  public Result<Double> calculatePv(Environment env, EquityIndexOptionTrade trade) {
    return calculateResult(env, trade, PV_CALC);
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
  public Result<DoubleMatrix1D> calculateBucketedPv01(Environment env, EquityIndexOptionTrade trade) {
    Result<StaticReplicationDataBundle> dataResult = _dataProviderFn.getEquityIndexDataProvider(env, trade);
    if (Result.allSuccessful(dataResult)) {
      StaticReplicationDataBundle bundle = dataResult.getValue();
      InstrumentDerivative derivative = createInstrumentDerivative(trade, env.getValuationTime());
      DoubleMatrix1D deltaBucketed = SENSITIVITY_CALC.calcDeltaBucketed(derivative, bundle);
      // Bucketed PV01 needs to be divided by 10000 to align with the scaling of the PV01
      double[] data = deltaBucketed.getData();
      double[] scaledData = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        scaledData[i] = data[i] / 10000;
      }
      DoubleMatrix1D scaled = new DoubleMatrix1D(scaledData);
      return Result.success(scaled);
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
