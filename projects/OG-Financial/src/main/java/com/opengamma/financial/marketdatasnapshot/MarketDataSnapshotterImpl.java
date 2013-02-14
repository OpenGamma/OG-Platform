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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.marketdata.ExternalIdBundleResolver;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link MarketDataSnapshotter}.
 */
public class MarketDataSnapshotterImpl implements MarketDataSnapshotter {
  // TODO: reimplement this in a javalike way, transliterating LINQ is dirty.

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotterImpl.class);

  private final ComputationTargetResolver _resolver;
  private final VolatilityCubeDefinitionSource _cubeDefinitionSource;
  private final YieldCurveSnapper _yieldCurveSnapper = new YieldCurveSnapper();
  private final VolatilitySurfaceSnapper _volatilitySurfaceSnapper = new VolatilitySurfaceSnapper();
  private final VolatilityCubeSnapper _volatilityCubeSnapper;
  @SuppressWarnings("rawtypes")
  private final StructuredSnapper[] _structuredSnappers;

  /**
   * @param resolver the target resolver, not null
   * @param cubeDefinitionSource The source of vol cube defns ( used to fill out the cube snapshots with nulls )
   */
  public MarketDataSnapshotterImpl(final ComputationTargetResolver resolver, final VolatilityCubeDefinitionSource cubeDefinitionSource) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
    _cubeDefinitionSource = cubeDefinitionSource;
    _volatilityCubeSnapper = new VolatilityCubeSnapper(_cubeDefinitionSource);
    _structuredSnappers = new StructuredSnapper[] {_yieldCurveSnapper, _volatilitySurfaceSnapper, _volatilityCubeSnapper };
  }

  @Override
  public StructuredMarketDataSnapshot createSnapshot(final ViewClient client, final ViewCycle cycle) {
    final CompiledViewDefinitionWithGraphs defn = cycle.getCompiledViewDefinition();
    final ComputationTargetResolver.AtVersionCorrection resolver = _resolver.atVersionCorrection(cycle.getResultModel().getVersionCorrection());
    return createSnapshot(new ExternalIdBundleResolver(resolver), cycle.getResultModel(), getGraphs(defn), cycle, defn.getViewDefinition().getName());
  }

  private Map<String, DependencyGraph> getGraphs(final CompiledViewDefinitionWithGraphs defn) {
    final HashMap<String, DependencyGraph> ret = new HashMap<String, DependencyGraph>();
    for (final CompiledViewCalculationConfiguration config : defn.getCompiledCalculationConfigurations()) {
      final String configName = config.getName();
      final DependencyGraph graph = defn.getDependencyGraphExplorer(configName).getWholeGraph();
      ret.put(configName, graph);
    }
    return ret;
  }

  public StructuredMarketDataSnapshot createSnapshot(final ExternalIdBundleResolver resolver, final ViewComputationResultModel results,
      final Map<String, DependencyGraph> graphs, final ViewCycle viewCycle, final String basisViewName) {
    final UnstructuredMarketDataSnapshot globalValues = getGlobalValues(resolver, results, graphs);

    final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = _yieldCurveSnapper.getValues(results, graphs, viewCycle);
    final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> surfaces = _volatilitySurfaceSnapper.getValues(results, graphs, viewCycle);
    final Map<VolatilityCubeKey, VolatilityCubeSnapshot> cubes = _volatilityCubeSnapper.getValues(results, graphs, viewCycle);

    final ManageableMarketDataSnapshot ret = new ManageableMarketDataSnapshot();
    ret.setBasisViewName(basisViewName);
    ret.setGlobalValues(globalValues);
    ret.setYieldCurves(yieldCurves);
    ret.setVolatilitySurfaces(surfaces);
    ret.setVolatilityCubes(cubes);
    return ret;
  }

  private UnstructuredMarketDataSnapshot getGlobalValues(final ExternalIdBundleResolver resolver, final ViewComputationResultModel results, final Map<String, DependencyGraph> graphs) {
    final Set<ComputedValue> data = results.getAllMarketData();
    final Multimap<MarketDataValueSpecification, ComputedValue> indexedData = identifyGlobalValues(resolver, data, graphs);
    final Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> dict = getGlobalValues(indexedData);
    final ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    snapshot.setValues(dict);
    return snapshot;
  }

  private Multimap<MarketDataValueSpecification, ComputedValue> identifyGlobalValues(final ExternalIdBundleResolver resolver, final Set<ComputedValue> data,
      final Map<String, DependencyGraph> graphs) {
    final Multimap<MarketDataValueSpecification, ComputedValue> indexedData = ArrayListMultimap.create();
    final Set<ComputedValue> dataFound = new HashSet<ComputedValue>();
    Set<ComputedValue> dataRemaining = null;
    for (final Entry<String, DependencyGraph> entry : graphs.entrySet()) {
      if (dataRemaining == null) {
        dataRemaining = data;
      } else {
        dataRemaining = Sets.difference(dataRemaining, dataFound);
      }
      final DependencyGraph graph = entry.getValue();
      for (final ComputedValue computedValue : dataRemaining) {
        final DependencyNode nodeProducing = graph.getNodeProducing(computedValue.getSpecification());
        if (nodeProducing != null && isTerminalUnstructuredOutput(nodeProducing, graph)) {
          dataFound.add(computedValue);
          final ExternalIdBundle identifiers = resolver.visitComputationTargetSpecification(nodeProducing.getRequiredMarketData().getTargetSpecification());
          if (identifiers != null) {
            // TODO: should use the order config to prioritise which scheme to select
            // TODO: we could add the config to the lookup to avoid passing two objects around
            // TODO: should we store all of the possible identifiers?
            indexedData.put(new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, identifiers.iterator().next()), computedValue);
          }
        }
      }
    }
    return indexedData;
  }

  private boolean isTerminalUnstructuredOutput(DependencyNode node, final DependencyGraph graph) {
    //This relies on two things in order to not suck
    // market data nodes are immediately fed into structured data nodes (so we only have to recurse 1 layer)
    // Whilst branching factor may be high, only a few of those paths will be to structured nodes, so we don't have to iterate too much
    // Chains from live data to each output are quite short

    final ArrayDeque<DependencyNode> remainingCandidates = new ArrayDeque<DependencyNode>(); //faster than Stack
    remainingCandidates.add(node);

    while (!remainingCandidates.isEmpty()) {
      node = remainingCandidates.remove();

      if (isStructuredNode(node)) {
        continue;
      }
      if (graph.getRootNodes().contains(node)) {
        return true;
      }
      for (final ValueSpecification output : node.getOutputValues()) {
        if (graph.getTerminalOutputSpecifications().contains(output)) {
          return true;
        }
      }
      remainingCandidates.addAll(node.getDependentNodes());
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  private boolean isStructuredNode(final DependencyNode node) {
    final Set<ValueSpecification> outputValues = node.getOutputValues();

    for (final ValueSpecification output : outputValues) {
      for (final StructuredSnapper snapper : _structuredSnappers) {
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

  private Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> getGlobalValues(final Multimap<MarketDataValueSpecification, ComputedValue> dataByTarget) {
    return Maps.transformValues(dataByTarget.asMap(),
        new Function<Collection<ComputedValue>, Map<String, ValueSnapshot>>() {

          @Override
          public Map<String, ValueSnapshot> apply(final Collection<ComputedValue> from) {
            final ImmutableListMultimap<String, ComputedValue> indexed = Multimaps.index(from, new Function<ComputedValue, String>() {
              @Override
              public String apply(final ComputedValue from) {
                return from.getSpecification().getValueName();
              }
            });
            return Maps.transformValues(indexed.asMap(), new Function<Collection<ComputedValue>, ValueSnapshot>() {

              @Override
              public ValueSnapshot apply(final Collection<ComputedValue> from) {
                final ComputedValue computedValue = Iterables.get(from, 0);
                return new ValueSnapshot((Double) computedValue.getValue());
              }

            });
          }
        });
  }

  // TODO: snapshot should be holding value specifications not value requirements

  @Override
  public Map<YieldCurveKey, Map<String, ValueRequirement>> getYieldCurveSpecifications(final ViewClient client, final ViewCycle cycle) {
    final CompiledViewDefinitionWithGraphs defn = cycle.getCompiledViewDefinition();
    final Map<String, DependencyGraph> graphs = getGraphs(defn);

    final Map<YieldCurveKey, Map<String, ValueRequirement>> ret = new HashMap<YieldCurveKey, Map<String, ValueRequirement>>();
    for (final Entry<String, DependencyGraph> entry : graphs.entrySet()) {
      final DependencyGraph graph = entry.getValue();
      for (final DependencyNode node : graph.getDependencyNodes()) {
        for (final ValueSpecification outputValue : node.getOutputValues()) {
          if (outputValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            addAll(ret, outputValue);
          } else if (outputValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
            final YieldCurveKey key = _yieldCurveSnapper.getKey(outputValue);
            add(ret, key, outputValue.toRequirementSpecification());
          }
        }
      }
    }
    return ret;
  }

  private void addAll(final Map<YieldCurveKey, Map<String, ValueRequirement>> ret, final ValueSpecification yieldCurveSpec) {
    final YieldCurveKey key = _yieldCurveSnapper.getKey(yieldCurveSpec);

    add(ret, key, yieldCurveSpec.toRequirementSpecification());

    //We know how the properties of this relate
    final ValueRequirement interpolatedSpec = new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE_INTERPOLATED, yieldCurveSpec.getTargetSpecification(),
        getCurveProperties(yieldCurveSpec));
    add(ret, key, interpolatedSpec);
  }

  private void add(final Map<YieldCurveKey, Map<String, ValueRequirement>> ret, final YieldCurveKey key, final ValueRequirement outputValue) {
    Map<String, ValueRequirement> ycMap = ret.get(key);
    if (ycMap == null) {
      ycMap = new HashMap<String, ValueRequirement>();
      ret.put(key, ycMap);
    }
    ycMap.put(outputValue.getValueName(), outputValue);
  }

  private ValueProperties getCurveProperties(final ValueSpecification curveSpec) {
    return ValueProperties.builder().with(ValuePropertyNames.CURVE, curveSpec.getProperty(ValuePropertyNames.CURVE)).get();
  }
}
