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

import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.CacheInvalidator;
import com.opengamma.sesame.cache.ValuationTimeCacheEntry;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link Environment} implementation created and managed by the engine.
 * This allows the engine to monitor what data from the environment is used
 * by each function and invalidate cache entries when the data changes. This
 * class is package-private because it is only intended to be used by the
 * engine itself, not user code.
 *
 * TODO can this be retired? might not need a cache or any cache invalidation after MarketDataEnvironment
 * could use SimpleEnvironment for everything
 */
final class EngineEnvironment implements Environment {

  // TODO an inner class used by all environment impls that is used for hashCode and equals
  // makes it explicit which parts of the environment are part of the cache key and which ones are ignored
  // TODO how should equality of MarketDataEnvironment be handled? requires some thought.

  /** The valuation time. */
  private final ZonedDateTime _valuationTime;

  /** The source of market data. */
  private final MarketDataBundle _marketDataBundle;

  /** Scenario arguments, keyed by the type of function implementation that uses them. */
  private final FilteredScenarioDefinition _scenarioDefinition;

  private final CacheInvalidator _cacheInvalidator;

  EngineEnvironment(ZonedDateTime valuationTime,
                    MarketDataBundle marketDataBundle,
                    CacheInvalidator cacheInvalidator) {
    this(valuationTime, marketDataBundle, FilteredScenarioDefinition.EMPTY, cacheInvalidator);
  }

  private EngineEnvironment(ZonedDateTime valuationTime,
                            MarketDataBundle marketDataBundle,
                            FilteredScenarioDefinition scenarioDefinition,
                            CacheInvalidator cacheInvalidator) {
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _scenarioDefinition = ArgumentChecker.notNull(scenarioDefinition, "scenarioDefinition");
    _marketDataBundle = ArgumentChecker.notNull(marketDataBundle, "marketDataBundle");
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

  // TODO wrap bundle to do cache invalidation?
  @Override
  public MarketDataBundle getMarketDataBundle() {
    return _marketDataBundle;
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
    return new EngineEnvironment(valuationTime,
                                 _marketDataBundle.withTime(valuationTime),
                                 _scenarioDefinition,
                                 _cacheInvalidator);
  }

  @Override
  public Environment withValuationTimeAndFixedMarketData(ZonedDateTime valuationTime) {
    return new EngineEnvironment(valuationTime, _marketDataBundle, _scenarioDefinition, _cacheInvalidator);
  }

  // TODO do we still need this?
  @Override
  public Environment withMarketData(MarketDataBundle marketDataBundle) {
    return new EngineEnvironment(_valuationTime, _marketDataBundle, _scenarioDefinition, _cacheInvalidator);
  }

  // TODO do we still need this?
  @Override
  public Environment withScenarioDefinition(FilteredScenarioDefinition scenarioDefinition) {
    return new EngineEnvironment(_valuationTime, _marketDataBundle, scenarioDefinition, _cacheInvalidator);
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
        _marketDataBundle.equals(that._marketDataBundle) &&
        _scenarioDefinition.equals(that._scenarioDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_valuationTime, _marketDataBundle, _scenarioDefinition);
  }

}
