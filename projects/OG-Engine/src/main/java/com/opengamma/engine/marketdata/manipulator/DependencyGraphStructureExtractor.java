/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Responsible for extracting structured objects from a dependency graph.
 * E.g. yield curve, volatility surface etc. The caller is able to specify what types of structure they are
 * interested in to avoid extraction of.
 */
public class DependencyGraphStructureExtractor {

  private final String _calcConfigName;

  private final MarketDataSelector _selector;

  private final SelectorResolver _selectorResolver;

  private final Map<ValueSpecification, DependencyNode> _graphValues = new HashMap<>();

  private final List<Pair<ValueSpecification, ValueSpecification>> _terminalValueRename = new LinkedList<>();

  private final Map<DistinctMarketDataSelector, Set<ValueSpecification>> _selectorMapping;

  private int _nodeDelta;

  /**
   * Create an extractor which will extract all structures matching the passed name.
   * 
   * @param calcConfigName the calculation configuration name, not null
   * @param selector the structures to be extracted, not null
   * @param selectorResolver the selector resolver, not null
   * @param selectorMapping populated with details of the manipulations inserted into the graph, not null
   */
  public DependencyGraphStructureExtractor(final String calcConfigName,
                                           final MarketDataSelector selector,
                                           final SelectorResolver selectorResolver,
                                           final Map<DistinctMarketDataSelector, Set<ValueSpecification>> selectorMapping) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(selector, "selectors");
    ArgumentChecker.notNull(selectorResolver, "selectorResolver");
    _calcConfigName = calcConfigName;
    _selector = selector;
    _selectorResolver = selectorResolver;
    _selectorMapping = selectorMapping;
  }

  /**
   * Tests if structure extraction is required for the given value specification.
   * 
   * @param valueSpecification the original value specification, not null
   * @return the set of value specifications associated with the manipulator; this should be updated with the rewritten value from the proxy node
   */
  // REVIEW Chris 2014-01-14 - Mutating the return value outside this class isn't great design. too obscure
  public Set<ValueSpecification> extractStructure(final ValueSpecification valueSpecification) {
    final DistinctMarketDataSelector matchingSelector =
        _selector.findMatchingSelector(valueSpecification, _calcConfigName, _selectorResolver);
    if (matchingSelector == null) {
      return null;
    }
    Set<ValueSpecification> values = _selectorMapping.get(matchingSelector);
    if (values == null) {
      values = new HashSet<>();
      _selectorMapping.put(matchingSelector, values);
    }
    return values;
  }

  public void storeProduction(final ValueSpecification valueSpec, final DependencyNode node) {
    _graphValues.put(valueSpec, node);
  }

  public DependencyNode getProduction(final ValueSpecification valueSpec) {
    return _graphValues.get(valueSpec);
  }

  public void addProxyValue(final ValueSpecification originalValue, final ValueSpecification proxyValue) {
    _terminalValueRename.add(Pair.of(originalValue, proxyValue));
    _nodeDelta++;
  }

  public void removeProxyValue(final ValueSpecification originalValue, final ValueSpecification proxyValue) {
    _terminalValueRename.remove(Pair.of(proxyValue, originalValue));
    _nodeDelta--;
  }

  public Iterable<Pair<ValueSpecification, ValueSpecification>> getTerminalValueRenames() {
    return _terminalValueRename;
  }

  public boolean hasTerminalValueRenames() {
    return !_terminalValueRename.isEmpty();
  }

  public int getNodeDelta() {
    return _nodeDelta;
  }

}
