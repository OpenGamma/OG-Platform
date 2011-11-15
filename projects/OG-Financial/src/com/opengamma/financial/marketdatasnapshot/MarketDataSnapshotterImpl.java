/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.marketdatasnapshot.MarketDataSnapshotter;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;

/**
 * Default implementation of {@link MarketDataSnapshotter}.
 */
public class MarketDataSnapshotterImpl implements MarketDataSnapshotter {
  // TODO: reimplement this in a javalike way, transliterating LINQ is dirty.

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotterImpl.class);
  
  private final VolatilityCubeDefinitionSource _cubeDefinitionSource;
  
  private final YieldCurveSnapper _yieldCurveSnapper = new YieldCurveSnapper();
  private final VolatilitySurfaceSnapper _volatilitySurfaceSnapper = new VolatilitySurfaceSnapper();
  private final VolatilityCubeSnapper _volatilityCubeSnapper;
  @SuppressWarnings("rawtypes")
  private final StructuredSnapper[] _structuredSnappers;

  /**
   * @param cubeDefinitionSource The source of vol cube defns ( used to fill out the cube snapshots with nulls ) 
   */
  public MarketDataSnapshotterImpl(VolatilityCubeDefinitionSource cubeDefinitionSource) {
    super();
    _cubeDefinitionSource = cubeDefinitionSource;
    _volatilityCubeSnapper =  new VolatilityCubeSnapper(_cubeDefinitionSource);
    _structuredSnappers = new StructuredSnapper[]{_yieldCurveSnapper, _volatilitySurfaceSnapper, _volatilityCubeSnapper };
  }

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
    Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> surfaces = _volatilitySurfaceSnapper.getValues(results, graphs, viewCycle);
    Map<VolatilityCubeKey, VolatilityCubeSnapshot> cubes = _volatilityCubeSnapper.getValues(results, graphs, viewCycle);
    
    ManageableMarketDataSnapshot ret = new ManageableMarketDataSnapshot();
    ret.setBasisViewName(basisViewName);
    ret.setGlobalValues(globalValues);
    ret.setYieldCurves(yieldCurves);
    ret.setVolatilitySurfaces(surfaces);
    ret.setVolatilityCubes(cubes);
    return ret;
  }

  private UnstructuredMarketDataSnapshot getGlobalValues(ViewComputationResultModel results, Map<String, DependencyGraph> graphs) {
    Set<ComputedValue> data = results.getAllMarketData();
    Set<ComputedValue> includedGlobalData = includedGlobalValues(data, graphs);
    
    ImmutableListMultimap<MarketDataValueSpecification, ComputedValue> dataByTarget = Multimaps.index(includedGlobalData,
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

  private Set<ComputedValue> includedGlobalValues(Set<ComputedValue> data, Map<String, DependencyGraph> graphs) {
    //TODO include nulls for missing values
    
    Set<ComputedValue> dataFound = new HashSet<ComputedValue>();
    
    Set<ComputedValue> dataRemaining = null;
    for (Entry<String, DependencyGraph> entry : graphs.entrySet()) {
      if (dataRemaining == null) {
        dataRemaining = data;
      } else {
        dataRemaining = Sets.difference(dataRemaining, dataFound);
      }

      DependencyGraph graph = entry.getValue();
      for (ComputedValue computedValue : dataRemaining) {
        DependencyNode nodeProducing = graph.getNodeProducing(computedValue.getSpecification());
        if (nodeProducing != null && isTerminalUnstructuredOutput(nodeProducing, graph)) {
          dataFound.add(computedValue);
        }
      }
    }
    
    return dataFound;
  }

  private boolean isTerminalUnstructuredOutput(DependencyNode node, DependencyGraph graph) {
    //This relies on two things in order to not suck
    // market data nodes are immediately fed into structured data nodes (so we only have to recurse 1 layer)
    // Whilst branching factor may be high, only a few of those paths will be to structured nodes, so we don't have to iterate too much
    // Chains from live data to each output are quite short
    
    ArrayDeque<DependencyNode> remainingCandidates = new ArrayDeque<DependencyNode>(); //faster than Stack
    remainingCandidates.add(node);
    
    while (!remainingCandidates.isEmpty()) {
      node = remainingCandidates.remove();
      
      if (isStructuredNode(node)) {
        continue;
      }
      if (graph.getRootNodes().contains(node)) {
        return true;
      }
      for (ValueSpecification output : node.getOutputValues()) {
        if (graph.getTerminalOutputSpecifications().contains(output)) {
          return true;
        }
      }
      remainingCandidates.addAll(node.getDependentNodes());
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  private boolean isStructuredNode(DependencyNode node) {
    Set<ValueSpecification> outputValues = node.getOutputValues();
    
    for (ValueSpecification output : outputValues) {
      for (StructuredSnapper snapper : _structuredSnappers) {
        if (output.getValueName() == snapper.getRequirementName()) {
          if (outputValues.size() != 1) {
            //TODO this is a bit fragile, but if this isn't true all sorts of things are broken
            s_logger.error("Structured market data node produced more than one output {} - {}", node, node.getOutputValues());
            throw new OpenGammaRuntimeException("Structured market data node produced more than one output");
          }
          return true;
        }
      }
    }
    
    return false;
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
            return Maps.transformValues(indexed.asMap(), new Function<Collection<ComputedValue>, ValueSnapshot>() {

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

  @Override
  public Map<YieldCurveKey, Map<String, ValueRequirement>> getYieldCurveSpecifications(ViewClient client, ViewCycle cycle) {
    CompiledViewDefinitionWithGraphs defn = cycle.getCompiledViewDefinition();
    Map<String, DependencyGraph> graphs = getGraphs(defn);

    Map<YieldCurveKey, Map<String, ValueRequirement>> ret = new HashMap<YieldCurveKey, Map<String, ValueRequirement>>();
    for (Entry<String, DependencyGraph> entry : graphs.entrySet()) {
      DependencyGraph graph = entry.getValue();
      for (DependencyNode node : graph.getDependencyNodes(ComputationTargetType.PRIMITIVE)) {
        for (ValueSpecification outputValue : node.getOutputValues()) {
          if (outputValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            addAll(ret, outputValue);
          }
        }
      }
    }
    return ret;
  }

  private void addAll(Map<YieldCurveKey, Map<String, ValueRequirement>> ret, ValueSpecification yieldCurveSpec) {
    YieldCurveKey key = _yieldCurveSnapper.getKey(yieldCurveSpec);

    add(ret, key, yieldCurveSpec.toRequirementSpecification());

    //This is a hack: the spec value will be pruned from the dep graph but will be in the computation cache,
    //  and we know how its properties relate to the sCurve value
    ValueRequirement specSpec = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC,
        yieldCurveSpec.getTargetSpecification(), getSpecProperties(yieldCurveSpec));
    add(ret, key, specSpec);

    //We know how the properties of this relate
    ValueRequirement interpolatedSpec = new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE_INTERPOLATED, yieldCurveSpec.getTargetSpecification(),
        getCurveProperties(yieldCurveSpec));
    add(ret, key, interpolatedSpec);
  }
  
  private void add(Map<YieldCurveKey, Map<String, ValueRequirement>> ret, YieldCurveKey key, ValueRequirement outputValue) {
    Map<String, ValueRequirement> ycMap = ret.get(key);
    if (ycMap == null) {
      ycMap = new HashMap<String, ValueRequirement>();
      ret.put(key, ycMap);
    }
    ycMap.put(outputValue.getValueName(), outputValue);
  }

  private ValueProperties getSpecProperties(ValueSpecification curveSpec) {
    return curveSpec.getProperties().withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
  }

  private ValueProperties getCurveProperties(ValueSpecification curveSpec) {
    return ValueProperties.builder().with(ValuePropertyNames.CURVE, curveSpec.getProperty(ValuePropertyNames.CURVE)).get();
  }
}
