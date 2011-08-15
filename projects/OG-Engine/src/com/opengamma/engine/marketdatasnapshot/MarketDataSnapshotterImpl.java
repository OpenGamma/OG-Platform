/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * Default implementation of {@link MarketDataSnapshotter}.
 * TODO: reimplement this in a javalike way, transliterating LINQ is dirty.
 */
public class MarketDataSnapshotterImpl implements MarketDataSnapshotter {

  private final YieldCurveSnapper _yieldCurveSnapper = new YieldCurveSnapper();
  @Override
  public StructuredMarketDataSnapshot createSnapshot(ViewClient client, ViewCycle cycle) {
    CompiledViewDefinitionWithGraphs defn = cycle.getCompiledViewDefinition();
    return createSnapshot(cycle.getResultModel(), getGraphs(defn), cycle, defn.getViewDefinition().getName());
  }

  private Map<String, DependencyGraph> getGraphs(CompiledViewDefinitionWithGraphs defn) {
    HashMap<String, DependencyGraph> ret = new HashMap<String, DependencyGraph>();
    for (CompiledViewCalculationConfiguration config : defn.getCompiledCalculationConfigurations()) {
      String configName = config.getName();
      DependencyGraph graph = defn.getDependencyGraphExplorer(configName).getWholeGraph();
      ret.put(configName, graph);
    }
    return ret;
  }

  public StructuredMarketDataSnapshot createSnapshot(ViewComputationResultModel results,
      Map<String, DependencyGraph> graphs, ViewCycle viewCycle, String basisViewName) {
    UnstructuredMarketDataSnapshot globalValues = getGlobalValues(results, graphs);

    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = _yieldCurveSnapper.getValues(results, graphs, viewCycle);
    /* TODO 
     *
    var volCubeDefinitions = CubeSnapper.GetValues(results, graphs, viewCycle, remoteEngineContext);
    var volSurfaceDefinitions = SurfaceSnapper.GetValues(results, graphs, viewCycle, remoteEngineContext);
    */
    ManageableMarketDataSnapshot ret = new ManageableMarketDataSnapshot();
    ret.setBasisViewName(basisViewName);
    ret.setGlobalValues(globalValues);
    ret.setYieldCurves(yieldCurves);
    return ret;
  }

  private UnstructuredMarketDataSnapshot getGlobalValues(ViewComputationResultModel results, Map<String, DependencyGraph> graphs) {
    Set<ComputedValue> data = results.getAllMarketData();
    //TODO var includedSpecs = GetIncludedGlobalSpecs(graphs);
    //TODO var includedGlobalData = data
    //TODO     .Where(d => includedSpecs.Contains(Tuple.Create(d.Specification.TargetSpecification, d.Specification.ValueName)));

    ImmutableListMultimap<MarketDataValueSpecification, ComputedValue> dataByTarget = Multimaps.index(data,
        new Function<ComputedValue, MarketDataValueSpecification>() {

          @Override
          public MarketDataValueSpecification apply(ComputedValue r) {
            ComputationTargetSpecification targetSpec = r.getSpecification().getTargetSpecification();
            return new MarketDataValueSpecification(getMarketType(targetSpec.getType()), targetSpec.getUniqueId());
          }
        });
        
    Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> dict = getGlobalValues(dataByTarget);

    ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    snapshot.setValues(dict);
    return snapshot;
  }

  private Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> getGlobalValues(ImmutableListMultimap<MarketDataValueSpecification, ComputedValue> dataByTarget) {
    return Maps.transformValues(dataByTarget.asMap(),
        new Function<Collection<ComputedValue>, Map<String, ValueSnapshot>>() {

          @Override
          public Map<String, ValueSnapshot> apply(Collection<ComputedValue> from) {
            ImmutableListMultimap<String, ComputedValue> indexed = Multimaps.index(from, new Function<ComputedValue, String>() {
              @Override
              public String apply(ComputedValue from) {
                return from.getSpecification().getValueName();
              }
            });
            return Maps.transformValues(indexed.asMap(), new Function<Collection<ComputedValue>, ValueSnapshot>(){

              @Override
              public ValueSnapshot apply(Collection<ComputedValue> from) {
                ComputedValue computedValue = Iterables.get(from, 0);
                return new ValueSnapshot((Double) computedValue.getValue());
              }
              
            });
          }
        });
  }

  private MarketDataValueType getMarketType(ComputationTargetType type) {
    switch (type) {
      case PRIMITIVE:
        return MarketDataValueType.PRIMITIVE;
      case SECURITY:
        return MarketDataValueType.SECURITY;
      case PORTFOLIO_NODE:
      case POSITION:
      case TRADE:
      default:
        throw new IllegalArgumentException("Unexpected market target type " + type);
    }
  }
  
}
