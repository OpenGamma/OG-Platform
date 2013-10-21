/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.cycle.ComputationCacheResponse;
import com.opengamma.engine.view.cycle.ComputationCycleQuery;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * Extracts some type of structured object from a cycle
 * 
 * @param <TKey> the key by which these snaps are ided
 * @param <TCalculatedValue> The type of value which comes out of the engine
 * @param <TSnapshot> The type of value which is stored in the snapshots
 */
public abstract class StructuredSnapper<TKey, TCalculatedValue, TSnapshot> {

  private static final Logger s_logger = LoggerFactory.getLogger(StructuredSnapper.class);

  private final String _requirementName;

  public StructuredSnapper(final String requirementName) {
    super();
    _requirementName = ValueRequirement.getInterned(requirementName);
  }

  public String getRequirementName() {
    return _requirementName;
  }

  public Map<TKey, TSnapshot> getValues(final ViewComputationResultModel results, final Map<String, DependencyGraph> graphs,
      final ViewCycle viewCycle) {
    final Map<TKey, TCalculatedValue> calculatedValues = getValues(viewCycle, graphs);

    final Map<TKey, TSnapshot> ret = new HashMap<TKey, TSnapshot>();
    for (final Entry<TKey, TCalculatedValue> entry : calculatedValues.entrySet()) {
      final TSnapshot snapshot = buildSnapshot(results, entry.getKey(), entry.getValue());
      ret.put(entry.getKey(), snapshot);
    }
    return ret;
  }

  protected static String getSingleProperty(final ValueSpecification spec, final String propertyName) {
    final ValueProperties properties = spec.getProperties();
    final Set<String> curves = properties.getValues(propertyName);
    if (curves.size() != 1) {
      throw new IllegalArgumentException("Couldn't find curve property from " + spec);
    }
    final String curve = Iterables.get(curves, 0);
    return curve;
  }

  private Map<TKey, TCalculatedValue> getValues(final ViewCycle viewCycle, final Map<String, DependencyGraph> dependencyGraphs) {
    final Map<String, Collection<ValueSpecification>> values = getMatchingSpecifications(dependencyGraphs, _requirementName);
    final Map<TKey, TCalculatedValue> ts = new HashMap<TKey, TCalculatedValue>();

    for (final Entry<String, Collection<ValueSpecification>> entry : values.entrySet()) {
      final Iterable<ValueSpecification> requiredSpecsIt = Iterables.filter(entry.getValue(),
          new Predicate<ValueSpecification>() {

            @Override
            public boolean apply(final ValueSpecification input) {
              return !ts.containsKey(getKey(input));
            }

          });
      final Collection<ValueSpecification> requiredSpecs = Lists.newArrayList(requiredSpecsIt);
      if (requiredSpecs.isEmpty()) {
        continue;
      }

      final ComputationCycleQuery cacheQuery = new ComputationCycleQuery();
      cacheQuery.setCalculationConfigurationName(entry.getKey());
      cacheQuery.setValueSpecifications(requiredSpecs);
      final ComputationCacheResponse computationCacheResponse = viewCycle.queryComputationCaches(cacheQuery);

      if (computationCacheResponse.getResults().size() != requiredSpecs.size()) {
        s_logger.debug("Failed to get all results from computation cache");
      }

      final Map<TKey, Pair<ValueSpecification, Object>> infos = Maps.uniqueIndex(computationCacheResponse.getResults(),
          new Function<Pair<ValueSpecification, Object>, TKey>() {

            @Override
            public TKey apply(final Pair<ValueSpecification, Object> from) {
              return getKey(from.getFirst());
            }
          });

      for (final Entry<TKey, Pair<ValueSpecification, Object>> result : infos.entrySet()) {
        @SuppressWarnings("unchecked")
        final TCalculatedValue calcValue = (TCalculatedValue) result.getValue().getSecond();
        ts.put(result.getKey(), calcValue);
      }
    }
    return ts;
  }

  private Map<String, Collection<ValueSpecification>> getMatchingSpecifications(final Map<String, DependencyGraph> graphs,
      final String specName) {
    final Map<String, Collection<ValueSpecification>> ret = new HashMap<String, Collection<ValueSpecification>>();
    for (final Entry<String, DependencyGraph> kvp : graphs.entrySet()) {
      final String config = kvp.getKey();
      final DependencyGraph graph = kvp.getValue();
      final Set<ValueSpecification> specsSet = new HashSet<ValueSpecification>();
      final Iterator<DependencyNode> nodes = graph.nodeIterator();
      while (nodes.hasNext()) {
        final DependencyNode node = nodes.next();
        final int count = node.getOutputCount();
        for (int i = 0; i < count; i++) {
          final ValueSpecification output = node.getOutputValue(i);
          // Value names are interned
          if (output.getValueName() == _requirementName) {
            specsSet.add(output);
          }
        }
      }
      ret.put(config, specsSet);
    }
    return ret;
  }

  protected static ManageableUnstructuredMarketDataSnapshot getUnstructured(final SnapshotDataBundle bundle) {
    final Set<Entry<ExternalIdBundle, Double>> bundlePoints = bundle.getDataPointSet();
    final ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    for (final Map.Entry<ExternalIdBundle, Double> bundlePoint : bundlePoints) {
      snapshot.putValue(bundlePoint.getKey(), MarketDataRequirementNames.MARKET_VALUE, ValueSnapshot.of(bundlePoint.getValue()));
    }
    return snapshot;
  }

  abstract TKey getKey(ValueSpecification spec);

  abstract TSnapshot buildSnapshot(ViewComputationResultModel resultModel, TKey key, TCalculatedValue calculated);
}
