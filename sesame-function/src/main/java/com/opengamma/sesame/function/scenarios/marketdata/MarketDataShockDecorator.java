/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.marketdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Function that decorates {@link MarketDataFn} and applies shocks to the underlying market data.
 */
public class MarketDataShockDecorator
    implements MarketDataFn, ScenarioFunction<MarketDataShock, MarketDataShockDecorator> {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataShockDecorator.class);

  /** The underlying market data function that this function decorates. */
  private final MarketDataFn _delegate;

  /**
   * @param delegate the function to decorate
   */
  public MarketDataShockDecorator(MarketDataFn delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Result<Double> getFxRate(Environment env, CurrencyPair currencyPair) {
    return _delegate.getFxRate(decorateDataSource(env), currencyPair);
  }

  @Override
  public Result<Double> getCurveNodeValue(Environment env, CurveNodeWithIdentifier node) {
    return _delegate.getCurveNodeValue(decorateDataSource(env), node);
  }

  @Override
  public Result<Double> getCurveNodeUnderlyingValue(Environment env, PointsCurveNodeWithIdentifier node) {
    return _delegate.getCurveNodeUnderlyingValue(decorateDataSource(env), node);
  }

  @Override
  public Result<Double> getMarketValue(Environment env, ExternalIdBundle id) {
    return _delegate.getMarketValue(decorateDataSource(env), id);
  }

  @Override
  public Result<?> getValue(Environment env, ExternalIdBundle id, FieldName fieldName) {
    return _delegate.getValue(decorateDataSource(env), id, fieldName);
  }

  private Environment decorateDataSource(Environment env) {
    List<MarketDataShock> shocks = env.getScenarioArguments(this);

    if (shocks.isEmpty()) {
      s_logger.debug("No shocks in the environment");
      return env;
    }
    return env.withMarketData(new DataSourceDecorator(env.getMarketDataSource(), shocks));
  }

  @Override
  public Class<MarketDataShock> getArgumentType() {
    return MarketDataShock.class;
  }

  private class DataSourceDecorator implements MarketDataSource {

    /** The decorated data source. */
    private final MarketDataSource _delegate;

    /** The shocks to apply to the market data. */
    private final List<MarketDataShock> _shocks;

    private DataSourceDecorator(MarketDataSource delegate, List<MarketDataShock> shocks) {
      _delegate = delegate;
      _shocks = shocks;
    }

    @Override
    public Result<?> get(ExternalIdBundle id, FieldName fieldName) {
      Result<?> result = _delegate.get(id, fieldName);

      if (!result.isSuccess()) {
        return result;
      }
      Object value = result.getValue();

      if (!(value instanceof Double)) {
        return Result.failure(FailureStatus.ERROR, "Market data shocks can only be applied to double values. Value " +
            "for {} is of type {}, value {}", id, value.getClass().getName(), value);
      }
      double shockedValue = (double) value;

      for (MarketDataShock shock : _shocks) {
        shockedValue = shock.apply(id, shockedValue);
      }
      return Result.success(shockedValue);
    }
  }
}


