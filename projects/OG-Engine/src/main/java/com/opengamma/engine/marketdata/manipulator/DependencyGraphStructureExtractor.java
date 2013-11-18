/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Responsible for extracting structured objects from a dependency graph. E.g. yield curve, volatility surface etc. The caller is able to specify what types of structure they are interested in to
 * avoid extraction of.
 */
public class DependencyGraphStructureExtractor {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphStructureExtractor.class);

  private final String _calcConfigName;

  private final MarketDataSelector _selectors;

  private final Map<String, NodeExtractor<?>> _nodeExtractors;

  private final SelectorResolver _selectorResolver;

  private final Map<ValueSpecification, DependencyNode> _graphValues = new HashMap<ValueSpecification, DependencyNode>();

  private final List<Pair<ValueSpecification, ValueSpecification>> _terminalValueRename = new LinkedList<Pair<ValueSpecification, ValueSpecification>>();

  private final Map<DistinctMarketDataSelector, Set<ValueSpecification>> _manipulators;

  private int _nodeDelta;

  /**
   * Create an extractor which will extract all structures matching the passed name.
   * 
   * @param calcConfigName the calculation configuration name, not null
   * @param selectors the structures to be extracted, not null
   * @param selectorResolver the selector resolver, not null
   * @param manipulators populated with details of the manipulations inserted into the graph, not null
   */
  public DependencyGraphStructureExtractor(final String calcConfigName, final MarketDataSelector selectors, final SelectorResolver selectorResolver,
      final Map<DistinctMarketDataSelector, Set<ValueSpecification>> manipulators) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(selectors, "selectors");
    ArgumentChecker.notNull(selectorResolver, "selectorResolver");
    _calcConfigName = calcConfigName;
    _selectors = selectors;
    _nodeExtractors = buildExtractors(selectors.getApplicableStructureTypes());
    _selectorResolver = selectorResolver;
    _manipulators = manipulators;
  }

  private Map<String, NodeExtractor<?>> buildExtractors(Set<StructureType> requiredStructureTypes) {
    Map<String, NodeExtractor<?>> extractors = new HashMap<>();
    for (StructureType structureType : requiredStructureTypes) {
      NodeExtractor<?> nodeExtractor = structureType.getNodeExtractor();
      if (nodeExtractor == null) {
        s_logger.warn("No extractor is currently available for structure type: {} - unable to perform manipulation", structureType);
      } else {
        extractors.put(nodeExtractor.getSpecificationName(), nodeExtractor);
      }
    }
    return Collections.unmodifiableMap(extractors);
  }

  /**
   * Tests if structure extraction is required for the given value specification.
   * 
   * @param valueSpecification the original value specification, not null
   * @return the set of value specifications associated with the manipulator; this should be updated with the rewritten value from the proxy node
   */
  public Set<ValueSpecification> extractStructure(final ValueSpecification valueSpecification) {
    final NodeExtractor<?> extractor = _nodeExtractors.get(valueSpecification.getValueName());
    if (extractor == null) {
      return null;
    }
    final StructureIdentifier<?> structureId = extractor.getStructuredIdentifier(valueSpecification);
    if (structureId == null) {
      return null;
    }
    final DistinctMarketDataSelector matchingSelector = _selectors.findMatchingSelector(structureId, _calcConfigName, _selectorResolver);
    if (matchingSelector == null) {
      return null;
    }
    Set<ValueSpecification> values = _manipulators.get(matchingSelector);
    if (values == null) {
      values = new HashSet<ValueSpecification>();
      _manipulators.put(matchingSelector, values);
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
