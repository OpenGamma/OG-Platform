/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
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
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.MarketDataSourcingFunction;
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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link MarketDataSnapshotter}.
 */
public class MarketDataSnapshotterImpl implements MarketDataSnapshotter {
  // TODO: reimplement this in a javalike way, transliterating LINQ is dirty.
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotterImpl.class);

  /** The computation target resolver */
  private final ComputationTargetResolver _resolver;
  /** The historical time series source */
  private final HistoricalTimeSeriesSource _htsSource;
  /** Snapshots yield curves */
  private final YieldCurveSnapper _yieldCurveSnapper = new YieldCurveSnapper();
  /** Snapshots curves */
  private final CurveSnapper _curveSnapper = new CurveSnapper();
  /** Snapshots volatility surfaces */
  private final VolatilitySurfaceSnapper _volatilitySurfaceSnapper = new VolatilitySurfaceSnapper();
  /** Snapshots volatility cubes */
  private final VolatilityCubeSnapper _volatilityCubeSnapper = new VolatilityCubeSnapper();
  @SuppressWarnings("rawtypes")
  /** Array of structured market data snappers */
  private final StructuredSnapper[] _structuredSnappers;
  /** The snapshot mode */
  private final Mode _mode;

  /**
   * Constructs a instance which produces structured market data snapshots.
   *
   * @param resolver the target resolver, not null
   * @param htsSource Must be specified if market data is inputted via HTS, may be null
   */
  public MarketDataSnapshotterImpl(final ComputationTargetResolver resolver, final HistoricalTimeSeriesSource htsSource) {
    this(resolver, htsSource, Mode.STRUCTURED);
  }

  /**
   * @param resolver the target resolver, not null
   * @param htsSource Must be specified if market data is inputted via HTS, may be null
   * @param mode whether to create a structured or flattened snapshot
   */
  public MarketDataSnapshotterImpl(final ComputationTargetResolver resolver, final HistoricalTimeSeriesSource htsSource, final Mode mode) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
    _htsSource = htsSource;
    _structuredSnappers = new StructuredSnapper[] {_yieldCurveSnapper, _curveSnapper, _volatilitySurfaceSnapper, _volatilityCubeSnapper };
    _mode = mode;
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
    final ManageableMarketDataSnapshot ret = new ManageableMarketDataSnapshot();
    ret.setBasisViewName(basisViewName);
    ret.setGlobalValues(globalValues);
    ret.setValuationTime(viewCycle.getExecutionOptions().getValuationTime());
    if (_mode == Mode.STRUCTURED) {
      final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = _yieldCurveSnapper.getValues(results, graphs, viewCycle);
      final Map<CurveKey, CurveSnapshot> curves = _curveSnapper.getValues(results, graphs, viewCycle);
      final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> surfaces = _volatilitySurfaceSnapper.getValues(results, graphs, viewCycle);
      final Map<VolatilityCubeKey, VolatilityCubeSnapshot> cubes = _volatilityCubeSnapper.getValues(results, graphs, viewCycle);
      ret.setYieldCurves(yieldCurves);
      ret.setCurves(curves);
      ret.setVolatilitySurfaces(surfaces);
      ret.setVolatilityCubes(cubes);
    } else {
      ret.setYieldCurves(Collections.<YieldCurveKey, YieldCurveSnapshot>emptyMap());
      ret.setCurves(Collections.<CurveKey, CurveSnapshot>emptyMap());
      ret.setVolatilitySurfaces(Collections.<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>emptyMap());
      ret.setVolatilityCubes(Collections.<VolatilityCubeKey, VolatilityCubeSnapshot>emptyMap());
    }
    return ret;
  }

  private ManageableUnstructuredMarketDataSnapshot getGlobalAndUnresolvedValues(final ExternalIdBundleResolver resolver, final ViewComputationResultModel results,
      final Map<String, DependencyGraph> graphs) {
    final ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    for (final Entry<String, DependencyGraph> graphEntry : graphs.entrySet()) {
      final DependencyGraph graph = graphEntry.getValue();
      final Collection<ComputedValue> marketData = results.getAllMarketData();
      final Map<ValueSpecification, ComputedValue> resolvedValues = Maps.newHashMapWithExpectedSize(marketData.size());
      for (final ComputedValue computedValue : marketData) {
        resolvedValues.put(computedValue.getSpecification(), computedValue);
      }
      final int roots = graph.getRootCount();
      final Map<ValueSpecification, ?> terminalOutputs = graph.getTerminalOutputs();
      for (int i = 0; i < roots; i++) {
        final DependencyNode root = graph.getRootNode(i);
        extractTerminalUnstructuredOutput(root, resolvedValues, resolver, true, terminalOutputs, snapshot);
      }
    }
    return snapshot;
  }

  private void extractTerminalUnstructuredOutput(final DependencyNode node, final Map<ValueSpecification, ComputedValue> resolvedValues, final ExternalIdBundleResolver resolver, boolean pathToRoot,
      final Map<ValueSpecification, ?> terminalOutputs, final ManageableUnstructuredMarketDataSnapshot snapshot) {
    final int inputs = node.getInputCount();
    if (inputs == 0) {
      if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        final int outputs = node.getOutputCount();
        for (int i = 0; i < outputs; i++) {
          final ValueSpecification value = node.getOutputValue(i);
          final ComputedValue resolvedValue = resolvedValues.get(value);
          if (resolvedValue != null) {
            if (pathToRoot || terminalOutputs.containsKey(value)) {
              final ExternalIdBundle identifiers = resolveExternalIdBundle(resolver, value);
              if (identifiers != null) {
                snapshot.putValue(identifiers, value.getValueName(), ValueSnapshot.of(resolvedValue.getValue()));
              }
            }
          } else {
            // Missing market data
            final ExternalIdBundle identifiers = resolveExternalIdBundle(resolver, value);
            if (identifiers != null) {
              snapshot.putValue(identifiers, value.getValueName(), null);
            }
          }
        }
      }
      return;
    }
    if (pathToRoot && isStructuredNode(node) && _mode == Mode.STRUCTURED) {
      pathToRoot = false;
    }
    for (int i = 0; i < inputs; i++) {
      extractTerminalUnstructuredOutput(node.getInputNode(i), resolvedValues, resolver, pathToRoot, terminalOutputs, snapshot);
    }
  }

  private ExternalIdBundle resolveExternalIdBundle(final ExternalIdBundleResolver resolver, final ValueSpecification valueSpec) {
    ExternalIdBundle identifiers = resolver.visitComputationTargetSpecification(valueSpec.getTargetSpecification());
    // if reading live data from hts, we need to lookup the externalIdBundle via the hts unique id
    if (identifiers == null && _htsSource != null && valueSpec.getTargetSpecification().getUniqueId() != null) {
      // try a lookup in hts
      identifiers = _htsSource.getExternalIdBundle(valueSpec.getTargetSpecification().getUniqueId());
    }
    return identifiers;
  }

  @SuppressWarnings("rawtypes")
  private boolean isStructuredNode(final DependencyNode node) {
    final int outputs = node.getOutputCount();
    for (int i = 0; i < outputs; i++) {
      final ValueSpecification output = node.getOutputValue(i);
      for (final StructuredSnapper snapper : _structuredSnappers) {
        if (output.getValueName() == snapper.getRequirementName()) {
          if (outputs != 1) {
            //TODO this is a bit fragile, but if this isn't true all sorts of things are broken
            s_logger.error("Structured market data node produced more than one output {} - {}", node, DependencyNodeImpl.getOutputValues(node));
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
      final Iterator<DependencyNode> nodes = graph.nodeIterator();
      while (nodes.hasNext()) {
        final DependencyNode node = nodes.next();
        final int outputs = node.getOutputCount();
        for (int i = 0; i < outputs; i++) {
          final ValueSpecification outputValue = node.getOutputValue(i);
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
