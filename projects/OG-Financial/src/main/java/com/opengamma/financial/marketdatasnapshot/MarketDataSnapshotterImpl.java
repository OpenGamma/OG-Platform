/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
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
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
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
  private final HistoricalTimeSeriesSource _htsSource;
  private final VolatilityCubeDefinitionSource _cubeDefinitionSource;
  private final YieldCurveSnapper _yieldCurveSnapper = new YieldCurveSnapper();
  private final CurveSnapper _curveSnapper = new CurveSnapper();
  private final VolatilitySurfaceSnapper _volatilitySurfaceSnapper = new VolatilitySurfaceSnapper();
  private final VolatilityCubeSnapper _volatilityCubeSnapper;
  @SuppressWarnings("rawtypes")
  private final StructuredSnapper[] _structuredSnappers;

  /**
   * @param resolver the target resolver, not null
   * @param cubeDefinitionSource The source of vol cube defns ( used to fill out the cube snapshots with nulls )
   * @param htsSource Must be specified if market data is inputted via HTS, may be null
   */
  public MarketDataSnapshotterImpl(final ComputationTargetResolver resolver, final VolatilityCubeDefinitionSource cubeDefinitionSource, final HistoricalTimeSeriesSource htsSource) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
    _htsSource = htsSource;
    _cubeDefinitionSource = cubeDefinitionSource;
    _volatilityCubeSnapper = new VolatilityCubeSnapper(_cubeDefinitionSource);
    _structuredSnappers = new StructuredSnapper[] {_yieldCurveSnapper, _curveSnapper, _volatilitySurfaceSnapper, _volatilityCubeSnapper };
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
    final ManageableUnstructuredMarketDataSnapshot globalValues = getGlobalAndUnresolvedValues(resolver, results, graphs);

    final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = _yieldCurveSnapper.getValues(results, graphs, viewCycle);
    final Map<CurveKey, CurveSnapshot> curves = _curveSnapper.getValues(results, graphs, viewCycle);
    final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> surfaces = _volatilitySurfaceSnapper.getValues(results, graphs, viewCycle);
    final Map<VolatilityCubeKey, VolatilityCubeSnapshot> cubes = _volatilityCubeSnapper.getValues(results, graphs, viewCycle);

    final ManageableMarketDataSnapshot ret = new ManageableMarketDataSnapshot();
    ret.setBasisViewName(basisViewName);
    ret.setGlobalValues(globalValues);
    ret.setYieldCurves(yieldCurves);
    ret.setCurves(curves);
    ret.setVolatilitySurfaces(surfaces);
    ret.setVolatilityCubes(cubes);
    return ret;
  }

  private ManageableUnstructuredMarketDataSnapshot getGlobalAndUnresolvedValues(final ExternalIdBundleResolver resolver, final ViewComputationResultModel results, 
      final Map<String, DependencyGraph> graphs) {
    final ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    for (final Entry<String, DependencyGraph> graphEntry : graphs.entrySet()) {
      final DependencyGraph graph = graphEntry.getValue();
      Set<ValueSpecification> resolvedValues = Sets.newHashSet();
      for (ComputedValue computedValue : results.getAllMarketData()) {
        resolvedValues.add(computedValue.getSpecification());
        final DependencyNode nodeProducing = graph.getNodeProducing(computedValue.getSpecification());
        if ((nodeProducing != null) && isTerminalUnstructuredOutput(nodeProducing, graph)) {
          ExternalIdBundle identifiers = resolveExternalIdBundle(resolver, computedValue.getSpecification());
          if (identifiers != null) {
            snapshot.putValue(identifiers, computedValue.getSpecification().getValueName(), new ValueSnapshot(computedValue.getValue()));
          }
        }
      }
      //missing values go over the wire as nulls
      SetView<ValueSpecification> missingValues = Sets.difference(graph.getAllRequiredMarketData(), resolvedValues);
      for (ValueSpecification missingValue : missingValues) {
        ExternalIdBundle missingExternalIdBundle = resolveExternalIdBundle(resolver, missingValue);
        snapshot.putValue(missingExternalIdBundle, missingValue.getValueName(), null);
      }
    }
    return snapshot;
  }

  private ExternalIdBundle resolveExternalIdBundle(final ExternalIdBundleResolver resolver, ValueSpecification valueSpec) {
    ExternalIdBundle identifiers = resolver.visitComputationTargetSpecification(valueSpec.getTargetSpecification());
    // if reading live data from hts, we need to lookup the externalIdBundle via the hts unique id
    if (identifiers == null && _htsSource != null && valueSpec.getTargetSpecification().getUniqueId() != null) {
      // try a lookup in hts
      identifiers = _htsSource.getExternalIdBundle(valueSpec.getTargetSpecification().getUniqueId());
    }
    return identifiers;
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
