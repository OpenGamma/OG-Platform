/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.marketdata;

import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SecurityId;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

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
  public Result<Double> getMarketValue(Environment env, Security security) {
    return _delegate.getMarketValue(decorateDataSource(env), security);
  }

  @Override
  public Result<Double> getMarketValue(Environment env, ExternalIdBundle id) {
    return _delegate.getMarketValue(decorateDataSource(env), id);
  }

  @Override
  public <T> Result<T> getValue(Environment env, Security security, FieldName fieldName, Class<T> valueType) {
    return _delegate.getValue(env, security, fieldName, valueType);
  }

  private Environment decorateDataSource(Environment env) {
    // this will probably become obsolete with the new scenario API
    List<MarketDataShock> shocks = env.getScenarioArguments(this);

    if (shocks.isEmpty()) {
      s_logger.debug("No shocks in the environment");
      return env;
    }
    return env.withMarketData(new BundleDecorator(env.getMarketDataBundle(), shocks));
  }

  @Override
  public Class<MarketDataShock> getArgumentType() {
    return MarketDataShock.class;
  }

  // TODO this will have to be migrated to use MarketDataEnvironment if we still need it at all
  private class BundleDecorator implements MarketDataBundle {

    /** The decorated data source. */
    private final MarketDataBundle _delegate;

    /** The shocks to apply to the market data. */
    private final List<MarketDataShock> _shocks;

    private BundleDecorator(MarketDataBundle delegate, List<MarketDataShock> shocks) {
      _delegate = delegate;
      _shocks = shocks;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T, I extends MarketDataId<T>> Result<T> get(I id, Class<T> dataType) {
      Result<T> result = _delegate.get(id, dataType);

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
        // TODO this will only work for MarketDataIds with an external ID - raw and security
        ExternalIdBundle idBundle = getIdBundle(id);
        if (idBundle != null) {
          shockedValue = shock.apply(idBundle, shockedValue);
        }
      }
      return (Result<T>) Result.success(shockedValue);
    }

    @Override
    public <T, I extends MarketDataId<T>> Result<DateTimeSeries<LocalDate, T>> get(
        I id,
        Class<T> dataType,
        LocalDateRange dateRange) {

      return _delegate.get(id, dataType, dateRange);
    }

    @Override
    public MarketDataBundle withTime(ZonedDateTime time) {
      return new BundleDecorator(_delegate.withTime(time), _shocks);
    }

    @Override
    public MarketDataBundle withDate(LocalDate date) {
      return new BundleDecorator(_delegate.withDate(date), _shocks);
    }
  }

  @Nullable
  private static ExternalIdBundle getIdBundle(MarketDataId id) {
    if (id instanceof RawId) {
      return ((RawId) id).getId();
    } else if (id instanceof SecurityId) {
      return ((SecurityId) id).getId();
    } else {
      return null;
    }
  }
}


