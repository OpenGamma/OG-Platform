/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;
import java.util.Objects;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.CacheInvalidator;
import com.opengamma.sesame.cache.ValuationTimeCacheEntry;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * {@link Environment} implementation created and managed by the engine.
 * This allows the engine to monitor what data from the environment is used
 * by each function and invalidate cache entries when the data changes. This
 * class is package-private because it is only intended to be used by the
 * engine itself, not user code.
 */
final class EngineEnvironment implements Environment {

  // TODO an inner class used by all environment impls that is used for hashCode and equals
  // makes it explicit which parts of the environment are part of the cache key and which ones are ignored

  /** The valuation time. */
  private final ZonedDateTime _valuationTime;

  private final CycleMarketDataFactory _cycleMarketDataFactory;

  /** The source of market data. */
  private final MarketDataSource _marketDataSource;

  /** Scenario arguments, keyed by the type of function implementation that uses them. */
  private final FilteredScenarioDefinition _scenarioDefinition;

  private final CacheInvalidator _cacheInvalidator;

  EngineEnvironment(ZonedDateTime valuationTime,
                    CycleMarketDataFactory cycleMarketDataFactory,
                    CacheInvalidator cacheInvalidator) {
    this(valuationTime,
         cycleMarketDataFactory,
         cycleMarketDataFactory.getPrimaryMarketDataSource(),
         FilteredScenarioDefinition.EMPTY,
         cacheInvalidator);
  }

  private EngineEnvironment(ZonedDateTime valuationTime,
                            CycleMarketDataFactory cycleMarketDataFactory,
                            MarketDataSource marketDataSource,
                            FilteredScenarioDefinition scenarioDefinition,
                            CacheInvalidator cacheInvalidator) {

    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _cycleMarketDataFactory = ArgumentChecker.notNull(cycleMarketDataFactory, "cycleMarketDataFactory");
    _scenarioDefinition = ArgumentChecker.notNull(scenarioDefinition, "scenarioDefinition");
    _marketDataSource = ArgumentChecker.notNull(marketDataSource, "marketDataSource");
    _cacheInvalidator = ArgumentChecker.notNull(cacheInvalidator, "cacheInvalidator");
  }

  @Override
  public LocalDate getValuationDate() {
    LocalDate valuationDate = _valuationTime.toLocalDate();
    _cacheInvalidator.register(new ValuationTimeCacheEntry.ValidOnCalculationDay(valuationDate));
    return valuationDate;
  }

  @Override
  public ZonedDateTime getValuationTime() {
    _cacheInvalidator.register(new ValuationTimeCacheEntry.ValidAtCalculationInstant(_valuationTime));
    return _valuationTime;
  }

  @Override
  public MarketDataSource getMarketDataSource() {
    return new MarketDataSource() {
      @Override
      public Result<?> get(ExternalIdBundle id, FieldName fieldName) {
        _cacheInvalidator.register(id);
        return _marketDataSource.get(id, fieldName);
      }
    };
  }

  @Override
  public <A extends ScenarioArgument<A, F>, F extends ScenarioFunction<A, F>> List<A> getScenarioArguments(
      ScenarioFunction<A, F> scenarioFunction) {
    return _scenarioDefinition.getArguments(scenarioFunction);
  }

  @Override
  public FilteredScenarioDefinition getScenarioDefinition() {
    return _scenarioDefinition;
  }

  @Override
  public Environment withValuationTime(ZonedDateTime valuationTime) {
    MarketDataSource marketDataSource = _cycleMarketDataFactory.getMarketDataSourceForDate(valuationTime);
    return new EngineEnvironment(valuationTime, _cycleMarketDataFactory, marketDataSource,
                                 _scenarioDefinition, _cacheInvalidator);
  }

  @Override
  public Environment withValuationTimeAndFixedMarketData(ZonedDateTime valuationTime) {
    return new EngineEnvironment(valuationTime, _cycleMarketDataFactory, _marketDataSource,
                                 _scenarioDefinition, _cacheInvalidator);
  }

  @Override
  public Environment withMarketData(MarketDataSource marketDataSource) {
    return new EngineEnvironment(_valuationTime, _cycleMarketDataFactory, marketDataSource,
                                 _scenarioDefinition, _cacheInvalidator);
  }

  @Override
  public Environment withScenarioDefinition(FilteredScenarioDefinition scenarioDefinition) {
    return new EngineEnvironment(_valuationTime,
                                 _cycleMarketDataFactory,
                                 _marketDataSource,
                                 scenarioDefinition,
                                 _cacheInvalidator);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EngineEnvironment that = (EngineEnvironment) o;
    return _valuationTime.equals(that._valuationTime) &&
        _marketDataSource.equals(that._marketDataSource) &&
        _scenarioDefinition.equals(that._scenarioDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_valuationTime, _marketDataSource, _scenarioDefinition);
  }

}
