/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.List;
import java.util.Objects;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.util.ArgumentChecker;

/**
 * <p>Simple immutable {@link Environment} implementation.
 * Functions should not create instances of this directly. If a function needs to modify the environment
 * before calling another function it should use the helper methods on the {@link Environment} interface, e.g.
 * {@link Environment#withValuationTime(ZonedDateTime)} etc.</p>
 *
 * <p>Instances should only be created in test cases or for passing to functions executing outside the engine.
 * If a function directly creates its own environments in a running engine it could dramatically affect performance
 * by preventing caching of shared values and forcing them to be recalculated every time they are used.</p>
 */
public final class SimpleEnvironment implements Environment {

  // TODO an inner class used by all environment impls that is used for hashCode and equals
  // makes it explicit which parts of the environment are part of the cache key and which ones are ignored

  /** The valuation time. */
  private final ZonedDateTime _valuationTime;

  /** The function that provides market data. */
  private final MarketDataSource _marketDataSource;

  /** Scenario definition. */
  private final FilteredScenarioDefinition _scenarioDefinition;

  public SimpleEnvironment(ZonedDateTime valuationTime,
                           MarketDataSource marketDataSource) {
    this(valuationTime, marketDataSource, FilteredScenarioDefinition.EMPTY);
  }

  public SimpleEnvironment(ZonedDateTime valuationTime,
                           MarketDataSource marketDataSource,
                           FilteredScenarioDefinition scenarioDefinition) {
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _marketDataSource = ArgumentChecker.notNull(marketDataSource, "marketDataSource");
    _scenarioDefinition = ArgumentChecker.notNull(scenarioDefinition, "scenarioDefinition");
  }

  @Override
  public LocalDate getValuationDate() {
    return _valuationTime.toLocalDate();
  }

  @Override
  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  @Override
  public MarketDataSource getMarketDataSource() {
    return _marketDataSource;
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
    return new SimpleEnvironment(
        ArgumentChecker.notNull(valuationTime, "valuationTime"), _marketDataSource, _scenarioDefinition);
  }

  @Override
  public Environment withValuationTimeAndFixedMarketData(ZonedDateTime valuationTime) {
    return new SimpleEnvironment(
        ArgumentChecker.notNull(valuationTime, "valuationTime"), _marketDataSource, _scenarioDefinition);
  }

  @Override
  public Environment withMarketData(MarketDataSource marketData) {
    return new SimpleEnvironment(_valuationTime, ArgumentChecker.notNull(marketData, "marketData"), _scenarioDefinition);
  }

  @Override
  public Environment withScenarioDefinition(FilteredScenarioDefinition scenarioDefinition) {
    return new SimpleEnvironment(_valuationTime, _marketDataSource, scenarioDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_valuationTime, _marketDataSource, _scenarioDefinition);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SimpleEnvironment other = (SimpleEnvironment) obj;
    return Objects.equals(this._valuationTime, other._valuationTime) &&
           Objects.equals(this._marketDataSource, other._marketDataSource) &&
           Objects.equals(this._scenarioDefinition, other._scenarioDefinition);
  }

  @Override
  public String toString() {
    return "SimpleEnvironment [" +
        "_valuationTime=" + _valuationTime +
        ", _marketDataSource=" + _marketDataSource +
        ", _scenarioArguments=" + _scenarioDefinition +
        "]";
  }
}
