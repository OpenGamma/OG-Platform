/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Encapsulates a set of transformations to apply to market data when a calculation cycle is run.
 */
public class Scenario {

  /** For parsing valuation time. */
  private static final DateTimeFormatter s_dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /** Manipulators keyed by the selectors for the items they apply to. */
  private final ListMultimap<DistinctMarketDataSelector, StructureManipulator<?>> _manipulations = ArrayListMultimap.create();

  /** This scenario's name. */
  private final String _name;
  /** The simulation to which this scenario belongs, possibly null */
  private final Simulation _simulation;

  /** Calc configs to which this scenario will be applied, null will match any config. */
  private Set<String> _calcConfigNames;
  /** Valuation time of this scenario's calculation cycle. */
  private Instant _valuationTime;
  /** Version correction used by the resolver. */
  private VersionCorrection _resolverVersionCorrection;

  /**
   * Creates a new scenario with a calcuation configuration name of "Default", valuation time of {@code Instant.now()}
   * and resolver version correction of {@link VersionCorrection#LATEST}.
   * @param name The scenario name, not null
   */
  public Scenario(String name) {
    ArgumentChecker.notEmpty(name, "name");
    _name = name;
    _simulation = null;
  }

  /* package */ Scenario(Simulation simulation, String name) {
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(simulation, "simulation");
    _simulation = simulation;
    _name = name;
  }

  /**
   * @return A object for specifying which curves should be transformed
   */
  public YieldCurveSelector.Builder curve() {
    return new YieldCurveSelector.Builder(this);
  }

  public YieldCurveDataSelectorBuilder curveData() {
    return new YieldCurveDataSelectorBuilder(this);
  }

  /**
   * @return An object for specifying which market data points should be transformed
   */
  public PointSelector.Builder marketDataPoint() {
    return new PointSelector.Builder(this);
  }

  /**
   * @return An object for specifying which volatility surfaces should be transformed
   */
  public VolatilitySurfaceSelector.Builder surface() {
    return new VolatilitySurfaceSelector.Builder(this);
  }

  public SpotRateSelectorBuilder spotRate() {
    return new SpotRateSelectorBuilder(this);
  }

  /**
   * Updates this scenario to apply to the specified calculation configuration.
   * @param configNames The calculation configuration name
   * @return The modified scenario
   */
  public Scenario calculationConfigurations(String... configNames) {
    ArgumentChecker.notEmpty(configNames, "configName");
    _calcConfigNames = ImmutableSet.copyOf(configNames);
    return this;
  }

  /**
   * Updates this scenario to use the specified valuation time.
   * @param valuationTime The valuation time
   * @return The modified scenario
   */
  public Scenario valuationTime(Instant valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _valuationTime = valuationTime;
    return this;
  }

  /**
   * Updates this scenario to use the specified valuation time.
   * @param valuationTime The valuation time
   * @return The modified scenario
   */
  public Scenario valuationTime(String valuationTime) {
    try {
      LocalDateTime localTime = LocalDateTime.parse(valuationTime, s_dateFormatter);
      _valuationTime = ZonedDateTime.of(localTime, ZoneOffset.UTC).toInstant();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Valuation time isn't in a valid format. Expected format " +
                                             "'yyyy-MM-dd HH:mm', value: " + valuationTime);
    }
    return this;
  }

  /**
   * Updates this scenario to use the specified valuation time.
   * @param valuationTime The valuation time
   * @return The modified scenario
   */
  public Scenario valuationTime(ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    return valuationTime(valuationTime.toInstant());
  }

  /**
   * Updates this scenario to use the specified version correction in the resolver.
   * @param resolverVersionCorrection The resolver version correction
   * @return The modified scenario
   */
  public Scenario resolverVersionCorrection(VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _resolverVersionCorrection = resolverVersionCorrection;
    return this;
  }

  /**
   * @return A {@link ScenarioDefinition} created from this scenario's selectors and manipulators
   */
  @SuppressWarnings("unchecked")
  public ScenarioDefinition createDefinition() {
    Map<DistinctMarketDataSelector, FunctionParameters> params = Maps.newHashMapWithExpectedSize(_manipulations.size());
    for (Map.Entry<DistinctMarketDataSelector, Collection<StructureManipulator<?>>> entry : _manipulations.asMap().entrySet()) {
      DistinctMarketDataSelector selector = entry.getKey();
      // ListMultimap always has Lists as entries even if the signature doesn't say so
      List<StructureManipulator<?>> manipulators = (List<StructureManipulator<?>>) entry.getValue();
      CompositeStructureManipulator compositeManipulator = new CompositeStructureManipulator(manipulators);
      SimpleFunctionParameters functionParameters = new SimpleFunctionParameters();
      functionParameters.setValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME, compositeManipulator);
      params.put(selector, functionParameters);
    }
    return new ScenarioDefinition(_name, params);
  }

  /* package */ void add(DistinctMarketDataSelector selector, StructureManipulator<?> manipulator) {
    _manipulations.put(selector, manipulator);
  }

  /* package */ Instant getValuationTime() {
    if (_valuationTime != null) {
      return _valuationTime;
    } else if (_simulation != null) {
      return _simulation.getValuationTime();
    } else {
      return null;
    }
  }

  /* package */ VersionCorrection getResolverVersionCorrection() {
    if (_resolverVersionCorrection != null) {
      return _resolverVersionCorrection;
    } else if (_simulation != null) {
      return _simulation.getResolverVersionCorrection();
    } else {
      return null;
    }
  }

  // TODO get from simulation if null
  /* package */ Set<String> getCalcConfigNames() {
    if (_calcConfigNames != null) {
      return _calcConfigNames;
    } else if (_simulation != null) {
      return _simulation.getCalcConfigNames();
    } else {
      return null;
    }
  }

  /**
   * @return The scenario name, not null
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_manipulations, _name, _calcConfigNames, _valuationTime, _resolverVersionCorrection);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Scenario other = (Scenario) obj;
    return Objects.equals(this._manipulations, other._manipulations) &&
        Objects.equals(this._name, other._name) &&
        Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._valuationTime, other._valuationTime) &&
        Objects.equals(this._resolverVersionCorrection, other._resolverVersionCorrection);
  }

  @Override
  public String toString() {
    return "Scenario [" +
        "_name='" + _name + "'" +
        ", _calcConfigNames=" + _calcConfigNames +
        ", _valuationTime=" + _valuationTime +
        ", _resolverVersionCorrection=" + _resolverVersionCorrection +
        ", _manipulations=" + _manipulations +
        "]";
  }
}
