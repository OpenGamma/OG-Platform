/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Performs manipulation of the compiled dependency graphs based on a general marketDataSelector (which is applied to all graphs) and a set of specific market data selectors (which is applied only to
 * named graphs). If the graph contains nodes that match the selectors, they will be updated such that proxy nodes are inserted which are able to intercept market data values and transform then.
 */
public class MarketDataSelectionGraphManipulator {

  private static final DependencyNodeFunction MANIPULATION_FUNCTION = DependencyNodeFunctionImpl.of(StructureManipulationFunction.INSTANCE);

  /**
   * The market data selector which will be applied to all graphs.
   */
  private final MarketDataSelector _marketDataSelector;

  /**
   * The selectors which will be applied only to named graphs.
   */
  private final Map<String, Set<MarketDataSelector>> _specificSelectors;

  /**
   * Component to include when caching compiled dependency graphs.
   */
  private final Serializable _cacheHintKey;

  private static final class CacheHintKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int _hashCode;
    private final Serializable _marketDataSelector;
    private final Serializable _specificSelectors;

    public CacheHintKey(final Serializable marketDataSelector, final Serializable specificSelectors) {
      _marketDataSelector = marketDataSelector;
      _specificSelectors = specificSelectors;
      _hashCode = marketDataSelector.hashCode() * 31 + specificSelectors.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CacheHintKey)) {
        return false;
      }
      final CacheHintKey other = (CacheHintKey) o;
      return _marketDataSelector.equals(other._marketDataSelector) && _specificSelectors.equals(other._specificSelectors);
    }

    @Override
    public int hashCode() {
      return _hashCode;
    }

    @Override
    public String toString() {
      return _marketDataSelector + " & " + _specificSelectors;
    }

  }

  /**
   * Constructor for the class taking the general and specific market data selectors.
   * 
   * @param marketDataSelector the market data selector which will be applied to all graphs, not null
   * @param specificSelectors the market data selectors which will be applied only to named graphs, not null
   */
  public MarketDataSelectionGraphManipulator(final MarketDataSelector marketDataSelector, final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificSelectors) {
    ArgumentChecker.notNull(marketDataSelector, "marketDataSelector");
    ArgumentChecker.notNull(specificSelectors, "specificSelectors");
    _marketDataSelector = marketDataSelector;
    final HashMap<String, Set<MarketDataSelector>> selectorsMap = new HashMap<String, Set<MarketDataSelector>>();
    for (Map.Entry<String, Map<DistinctMarketDataSelector, FunctionParameters>> entry : specificSelectors.entrySet()) {
      final Set<MarketDataSelector> selectors = new HashSet<>();
      for (MarketDataSelector selector : entry.getValue().keySet()) {
        selectors.add(selector);
      }
      selectorsMap.put(entry.getKey(), selectors);
    }
    _specificSelectors = selectorsMap;
    _cacheHintKey = new CacheHintKey(marketDataSelector, selectorsMap);
  }

  private DependencyNode modifyDependencyNode(DependencyNode node, ValueSpecification desiredOutput, DependencyGraphStructureExtractor extractor) {
    DependencyNode newNode = extractor.getProduction(desiredOutput);
    if (newNode != null) {
      return newNode;
    }
    final int inputs = node.getInputCount();
    if ((inputs == 1) && StructureManipulationFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
      // Found an existing proxy node
      final ValueSpecification inputValue = node.getInputValue(0);
      if (extractor.extractStructure(inputValue) == null) {
        // This proxy is no longer required
        newNode = modifyDependencyNode(node.getInputNode(0), inputValue, extractor);
        extractor.storeProduction(node.getOutputValue(0), newNode);
        extractor.removeProxyValue(inputValue, node.getOutputValue(0));
        return newNode;
      }
      // Store this as a valid proxy for the input node
      extractor.storeProduction(inputValue, node);
      extractor.storeProduction(node.getOutputValue(0), node);
      // TODO: Should be checking the inputs to the node - the structured identifiers might have changed and the graph below needs updating
      return node;
    }
    // Recurse into the inputs to this node
    DependencyNode[] newInputNodes = null;
    ValueSpecification[] newInputValues = null;
    for (int i = 0; i < inputs; i++) {
      final DependencyNode oldInputNode = (newInputNodes != null) ? newInputNodes[i] : node.getInputNode(i);
      final DependencyNode newInputNode = modifyDependencyNode(oldInputNode, node.getInputValue(i), extractor);
      if (newInputNode != oldInputNode) {
        if (newInputNodes == null) {
          newInputNodes = DependencyNodeImpl.getInputNodeArray(node);
          newInputValues = DependencyNodeImpl.getInputValueArray(node);
        }
        newInputNodes[i] = newInputNode;
        if (StructureManipulationFunction.UNIQUE_ID.equals(newInputNode.getFunction().getFunctionId())) {
          // New input node is a proxy; take the only output from it
          newInputValues[i] = newInputNode.getOutputValue(0);
        } else if (StructureManipulationFunction.UNIQUE_ID.equals(oldInputNode.getFunction().getFunctionId())) {
          // Old input node was a proxy; take the only input from it since we're now connected to the original node
          newInputValues[i] = oldInputNode.getInputValue(0);
        }
      }
    }
    if (newInputNodes == null) {
      // No changes to the node or the tree below it
      newNode = node;
    } else {
      // Tree underneath has changed; allocate the new node
      newNode = DependencyNodeImpl.of(node.getFunction(), node.getTarget(), DependencyNodeImpl.getOutputValueArray(node), newInputValues, newInputNodes);
    }
    // Check whether the node requires a proxy
    final int outputs = node.getOutputCount();
    DependencyNode result = newNode;
    for (int i = 0; i < outputs; i++) {
      final ValueSpecification output = node.getOutputValue(i);
      final Set<ValueSpecification> proxySpecs = extractor.extractStructure(output);
      if (proxySpecs != null) {
        final ComputationTargetSpecification target = node.getTarget();
        final ValueProperties properties = output.getProperties();
        final String originalFunction = properties.getStrictValue(ValuePropertyNames.FUNCTION);
        final ValueSpecification proxyOutput = new ValueSpecification(output.getValueName(), target, properties.copy().withoutAny(ValuePropertyNames.FUNCTION)
            .with(ValuePropertyNames.FUNCTION, originalFunction + StructureManipulationFunction.UNIQUE_ID).get());
        // REVIEW Chris 2014-01-14 - This isn't a good design. it's deliberately mutating the internal state of the extractor
        proxySpecs.add(proxyOutput);
        final DependencyNode proxyNode = new DependencyNodeImpl(MANIPULATION_FUNCTION, target, Collections.singleton(proxyOutput), Collections.singletonMap(output, newNode));
        extractor.storeProduction(proxyOutput, proxyNode);
        extractor.storeProduction(output, proxyNode);
        extractor.addProxyValue(output, proxyOutput);
        if (desiredOutput.equals(output)) {
          result = proxyNode;
        }
      } else {
        extractor.storeProduction(output, newNode);
      }
    }
    return result;
  }

  /**
   * Processes the specified graph, identifying any nodes which meet the selection criteria of the market data selectors. Those which do match will have new nodes inserted into the graph, proxying the
   * original nodes, and providing the ability to perform transformations on the data as required.
   * 
   * @param graph the graph to inspect, not null
   * @param resolver for looking up data used in selection criteria, for example securities, not null
   * @param selectorMapping populated with details of the manipulations inserted into the graph, not null
   * @return the new graph, not null
   */
  public DependencyGraph modifyDependencyGraph(final DependencyGraph graph, final ComputationTargetResolver.AtVersionCorrection resolver,
      final Map<DistinctMarketDataSelector, Set<ValueSpecification>> selectorMapping) {
    ArgumentChecker.notNull(graph, "graph");
    ArgumentChecker.notNull(resolver, "resolver");
    ArgumentChecker.notNull(selectorMapping, "selectorMapping");
    final String configurationName = graph.getCalculationConfigurationName();
    final MarketDataSelector combinedSelector = buildCombinedSelector(configurationName);
    // Drop out immediately if we have no shifts specified (caller should already have verified
    // this for the primary selector)
    if (!combinedSelector.hasSelectionsDefined()) {
      return graph;
    }
    final int roots = graph.getRootCount();
    final Set<DependencyNode> newRoots = Sets.newHashSetWithExpectedSize(roots);
    final DefaultSelectorResolver selectorResolver = new DefaultSelectorResolver(resolver);
    final DependencyGraphStructureExtractor extractor = new DependencyGraphStructureExtractor(configurationName, combinedSelector, selectorResolver, selectorMapping);
    for (int i = 0; i < roots; i++) {
      final DependencyNode root = graph.getRootNode(i);
      newRoots.add(modifyDependencyNode(root, root.getOutputValue(0), extractor));
    }
    final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs;
    if (extractor.hasTerminalValueRenames()) {
      terminalOutputs = new HashMap<>(graph.getTerminalOutputs());
      for (Pair<ValueSpecification, ValueSpecification> rename : extractor.getTerminalValueRenames()) {
        final Set<ValueRequirement> terminals = terminalOutputs.remove(rename.getFirst());
        if (terminals != null) {
          terminalOutputs.put(rename.getSecond(), terminals);
        }
      }
    } else {
      terminalOutputs = graph.getTerminalOutputs();
    }
    return new DependencyGraphImpl(graph.getCalculationConfigurationName(), newRoots, graph.getSize() + extractor.getNodeDelta(), terminalOutputs);
  }

  private MarketDataSelector buildCombinedSelector(String configurationName) {
    Set<MarketDataSelector> selectors = new HashSet<>(extractSpecificSelectors(configurationName));
    selectors.add(_marketDataSelector);
    return CompositeMarketDataSelector.of(selectors);
  }

  private Set<MarketDataSelector> extractSpecificSelectors(String configurationName) {
    return _specificSelectors.containsKey(configurationName) ? _specificSelectors.get(configurationName) : new HashSet<MarketDataSelector>();
  }

  public boolean hasManipulationsDefined() {
    return _marketDataSelector.hasSelectionsDefined() || !_specificSelectors.isEmpty();
  }

  public Serializable getCacheHintKey() {
    return _cacheHintKey;
  }

}
