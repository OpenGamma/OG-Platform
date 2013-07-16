/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Responsible for extracting structured objects from a dependency graph. E.g. yield curve,
 * volatility surface etc. The caller is able to specify what types of structure they are
 * interested in to avoid extraction of
 */
public class DependencyGraphStructureExtractor {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphStructureExtractor.class);

  /**
   * The dependency graph to examine for structures.
   */
  private final DependencyGraph _graph;


  private final Map<String, NodeExtractor<?>> _nodeExtractors;

  /**
   * Create an extractor which will extract all structures matching the passed name in the graph.
   *
   * @param graph the graph to examine
   * @param requiredStructureTypes the set of structure types to be extracted, neither null nor empty
   */
  public DependencyGraphStructureExtractor(DependencyGraph graph,
                                           Set<StructureType> requiredStructureTypes) {

    ArgumentChecker.notNull(graph, "graph");
    ArgumentChecker.notEmpty(requiredStructureTypes, "requiredStructureTypes");

    _graph = graph;
    _nodeExtractors = buildExtractors(requiredStructureTypes);
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
   * Extracts the structures (yield curves, vol surfaces etc) that are being used by
   * the dependency graphs. Returns a map containing an id for the structure
   * and the graph node where the structure is created.
   *
   * @return map of structured type key to the relevant dependency node
   */
  public Map<StructureIdentifier<?>, DependencyNode> extractStructures() {

    Map<StructureIdentifier<?>, DependencyNode> structures = new HashMap<>();

    for (DependencyNode node : _graph.getDependencyNodes()) {

      for (ValueSpecification valueSpecification : node.getOutputValues()) {

        String valueName = valueSpecification.getValueName();
        if (_nodeExtractors.containsKey(valueName)) {

          StructureIdentifier<?> id = _nodeExtractors.get(valueName).getStructuredIdentifier(node);
          if (id != null) {
            structures.put(id, node);
          }
        }
      }
    }

    return Collections.unmodifiableMap(structures);
  }
}
