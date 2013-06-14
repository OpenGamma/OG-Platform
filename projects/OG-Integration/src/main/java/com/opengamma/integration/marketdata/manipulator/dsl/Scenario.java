/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.google.common.collect.ArrayListMultimap;
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
 * TODO valuation time
 * TODO resolver version correction
 */
public class Scenario {

  /** Default calculation configuration name. TODO does this exist as a constant somewhere else? */
  private static final String DEFAULT = "Default";

  // TODO is it right that a Scenario only applies to a single calc config? should this be a list?
  private String _calcConfigName = DEFAULT;
  private Instant _valuationTime = Instant.now();
  private VersionCorrection _resolverVersionCorrection = VersionCorrection.LATEST;

  public Scenario() {
  }

  /* package */ Scenario(String calcConfigName, Instant valuationTime, VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _calcConfigName = calcConfigName;
    _valuationTime = valuationTime;
    _resolverVersionCorrection = resolverVersionCorrection;
  }

  /**
   * Manipulators keyed by the selectors for the items they apply to.
   */
  private final ListMultimap<DistinctMarketDataSelector, StructureManipulator<?>> _manipulations = ArrayListMultimap.create();

  /**
   * @return A object for specifying which curves should be transformed
   */
  public CurveSelector.Builder curve() {
    return new CurveSelector.Builder(this, _calcConfigName);
  }

  /**
   * @return An object for specifying which market data points should be transformed
   */
  public PointSelector.Builder marketDataPoint() {
    return new PointSelector.Builder(this, _calcConfigName);
  }

  public Scenario calculationConfig(String configName) {
    ArgumentChecker.notEmpty(configName, "configName");
    _calcConfigName = configName;
    return this;
  }

  public Scenario valuationTime(Instant valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _valuationTime = valuationTime;
    return this;
  }

  public Scenario resolverVersionCorrection(VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _resolverVersionCorrection = resolverVersionCorrection;
    return this;
  }

  /**
   * @return A {@link ScenarioDefinition} created from this scenario's selectors and manipulators.
   */
  @SuppressWarnings("unchecked")
  public ScenarioDefinition createDefinition() {
    Map<DistinctMarketDataSelector, FunctionParameters> params = Maps.newHashMapWithExpectedSize(_manipulations.size());
    for (Map.Entry<DistinctMarketDataSelector, Collection<StructureManipulator<?>>> entry : _manipulations.asMap().entrySet()) {
      // ListMultimap always has Lists as entries even if the signature doesn't say so
      DistinctMarketDataSelector selector = entry.getKey();
      List<StructureManipulator<?>> manipulators = (List<StructureManipulator<?>>) entry.getValue();
      CompositeStructureManipulator compositeManipulator = new CompositeStructureManipulator(manipulators);
      SimpleFunctionParameters functionParameters = new SimpleFunctionParameters();
      functionParameters.setValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME, compositeManipulator);
      params.put(selector, functionParameters);
    }
    return new ScenarioDefinition(params);
  }

  /* package */ void add(DistinctMarketDataSelector selector, StructureManipulator<?> manipulator) {
    _manipulations.put(selector, manipulator);
  }

  /* package */ Instant getValuationTime() {
    return _valuationTime;
  }

  /* package */ VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }
}
