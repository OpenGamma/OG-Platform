/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * Extracts some type of structured object from a cycle 
 * @param <TKey> the key by which these snaps are ided
 * @param <TCalculatedValue> The type of value which comes out of the engine
 * @param <TSnapshot> The type of value which is stored in the snapshots
 */
public abstract class StructuredSnapper<TKey, TCalculatedValue, TSnapshot> {

  private static final Logger s_logger = LoggerFactory.getLogger(StructuredSnapper.class);
  
  private final String _requirementName;

  public StructuredSnapper(String requirementName) {
    super();
    _requirementName = requirementName.intern();
  }

  public String getRequirementName() {
    return _requirementName;
  }


  public Map<TKey, TSnapshot> getValues(final ViewComputationResultModel results, Map<String, DependencyGraph> graphs,
      ViewCycle viewCycle) {
    Map<TKey, TCalculatedValue> calculatedValues = getValues(viewCycle, graphs);

    Map<TKey, TSnapshot> ret = new HashMap<TKey, TSnapshot>();
    for (Entry<TKey, TCalculatedValue> entry : calculatedValues.entrySet()) {
      TSnapshot snapshot = buildSnapshot(results, entry.getKey(), entry.getValue());
      ret.put(entry.getKey(), snapshot);
    }
    return ret;
  }
  
  protected static String getSingleProperty(ValueSpecification spec, String propertyName) {
    ValueProperties properties = spec.getProperties();
    Set<String> curves = properties.getValues(propertyName);
    if (curves.size() != 1) {
      throw new IllegalArgumentException("Couldn't find curve property from " + spec);
    }
    String curve = Iterables.get(curves, 0);
    return curve;
  }

  private Map<TKey, TCalculatedValue> getValues(ViewCycle viewCycle, Map<String, DependencyGraph> dependencyGraphs) {
    Map<String, Collection<ValueSpecification>> values = getMatchingSpecifications(dependencyGraphs, _requirementName);
    final Map<TKey, TCalculatedValue> ts = new HashMap<TKey, TCalculatedValue>();

    for (Entry<String, Collection<ValueSpecification>> entry : values.entrySet()) {
      Iterable<ValueSpecification> requiredSpecsIt = Iterables.filter(entry.getValue(),
          new Predicate<ValueSpecification>() {

            @Override
            public boolean apply(ValueSpecification input) {
              return !ts.containsKey(getKey(input));
            }

          });
      Collection<ValueSpecification> requiredSpecs = Lists.newArrayList(requiredSpecsIt);
      if (requiredSpecs.isEmpty()) {
        continue;
      }

      ComputationCacheQuery cacheQuery = new ComputationCacheQuery();
      cacheQuery.setCalculationConfigurationName(entry.getKey());
      cacheQuery.setValueSpecifications(requiredSpecs);
      ComputationCacheResponse computationCacheResponse = viewCycle.queryComputationCaches(cacheQuery);

      if (computationCacheResponse.getResults().size() != requiredSpecs.size()) {
        s_logger.debug("Failed to get all results from computation cache");
      }

      Map<TKey, Pair<ValueSpecification, Object>> infos = Maps.uniqueIndex(computationCacheResponse.getResults(),
          new Function<Pair<ValueSpecification, Object>, TKey>() {

            @Override
            public TKey apply(Pair<ValueSpecification, Object> from) {
              return getKey(from.getFirst());
            }
          });

      for (Entry<TKey, Pair<ValueSpecification, Object>> result : infos.entrySet()) {
        @SuppressWarnings("unchecked")
        TCalculatedValue calcValue = (TCalculatedValue) result.getValue().getSecond();
        ts.put(result.getKey(), calcValue);
      }
    }
    return ts;
  }

  private Map<String, Collection<ValueSpecification>> getMatchingSpecifications(Map<String, DependencyGraph> graphs,
      String specName) {
    Map<String, Collection<ValueSpecification>> ret = new HashMap<String, Collection<ValueSpecification>>();

    for (Entry<String, DependencyGraph> kvp : graphs.entrySet()) {
      String config = kvp.getKey();
      DependencyGraph graph = kvp.getValue();

      Set<DependencyNode> nodes = graph.getDependencyNodes();
      Iterable<Iterable<ValueSpecification>> specs = Iterables.transform(nodes,
          new Function<DependencyNode, Iterable<ValueSpecification>>() {

            @Override
            public Iterable<ValueSpecification> apply(DependencyNode input) {
              return Iterables.filter(input.getOutputValues(), new Predicate<ValueSpecification>() {

                @Override
                public boolean apply(ValueSpecification input) {
                  return input.getValueName() == _requirementName; // Should be interned
                }
              });
            }
          });
      Set<ValueSpecification> specsSet = new HashSet<ValueSpecification>();
      for (Iterable<ValueSpecification> group : specs) {
        for (ValueSpecification valueSpecification : group) {
          specsSet.add(valueSpecification);
        }
      }
      ret.put(config, specsSet);
    }
    return ret;
  }

  protected static ManageableUnstructuredMarketDataSnapshot getUnstructured(SnapshotDataBundle bundle) {
    Set<Entry<UniqueId, Double>> bundlePoints = bundle.getDataPoints().entrySet();
    ImmutableMap<MarketDataValueSpecification, Entry<UniqueId, Double>> bySpec =
      Maps.uniqueIndex(bundlePoints, new Function<Entry<UniqueId, Double>, MarketDataValueSpecification>() {
        @Override
        public MarketDataValueSpecification apply(Entry<UniqueId, Double> from) {
          return new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, from.getKey());
        }
      });
    Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> data = Maps.transformValues(bySpec,
        new Function<Entry<UniqueId, Double>, Map<String, ValueSnapshot>>() {

          @Override
          public Map<String, ValueSnapshot> apply(Entry<UniqueId, Double> from) {
            HashMap<String, ValueSnapshot> ret = new HashMap<String, ValueSnapshot>();
            ret.put(MarketDataRequirementNames.MARKET_VALUE, new ValueSnapshot(from.getValue()));
            return ret;
          }
        });
    ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    snapshot.setValues(data);
    return snapshot;
  }
  
  abstract TKey getKey(ValueSpecification spec);

  abstract TSnapshot buildSnapshot(ViewComputationResultModel resultModel, TKey key, TCalculatedValue calculated);
}
