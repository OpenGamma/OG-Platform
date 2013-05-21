/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Responsible for extracting structured objects from a dependency graph. E.g. yield curve,
 * volatility surface etc.
 *
 * @param <K> Identifier key for a structured market data value
 */
public abstract class AbstractDependencyGraphStructureExtractor<K extends StructuredMarketDataKey> {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDependencyGraphStructureExtractor.class);

  /**
   * The name of the value specification node to check for.
   */
  private final String _specificationName;

  /**
   * The dependency graph to examine for structures.
   */
  private final DependencyGraph _graph;

  /**
   * Create an extractor which will extract all structures matching the passed name in the graph.
   *
   * @param specificationName the specification name to check for
   * @param graph the graph to examine
   */
  public AbstractDependencyGraphStructureExtractor(String specificationName, DependencyGraph graph) {
    _specificationName = specificationName;
    _graph = graph;
  }

  /**
   * Extracts the structures (yield curves, vol surfaces etc) that are being used by
   * the dependency graphs. Returns a map containing the key to the structure
   * (dependent on subtype) and the graph node where the structure is created.
   *
   * @return map of structured type key to the relevant dependency node
   */
  public Map<K, DependencyNode> extractStructures() {

    Map<K, DependencyNode> structures = new HashMap<>();

    for (DependencyNode node : _graph.getDependencyNodes()) {

      for (ValueSpecification valueSpecification : node.getOutputValues()) {

        // Check that the node we're looking at is not a manipulator, nor that a manipulator
        // is already in place as a dependency
        if (nodeMatchesRequirement(node, valueSpecification)) {

          // There really shouldn't be an existing value, but just in case there is we capture it
          K structuredKey = getStructuredKey(valueSpecification);
          DependencyNode previous = structures.put(structuredKey, node);
          if (previous != null) {
            s_logger.warn("Replacing an existing node producing key: {} - {}", structuredKey, previous);
          }
        }
      }
    }

    return Collections.unmodifiableMap(structures);
  }

  private boolean nodeMatchesRequirement(DependencyNode node, ValueSpecification valueSpecification) {

    // Check spec name matches what we want ...
    if (_specificationName.equals(valueSpecification.getValueName()) &&
        // but that it's not a manipulation node we've added already
        valueSpecification.getProperty("MANIPULATION_MODE") == null) {

        // nor that it already has a dependent manipulation node
      for (DependencyNode dependent : node.getDependentNodes()) {

        for (ValueSpecification outputValue : dependent.getOutputValues()) {

          if (_specificationName.equals(outputValue) && outputValue.getProperty("MANIPULATION_MODE") != null) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Gets the structured key from the passed value specification. The specification will previously have
   * been matched against the configured specification name so can be asseume to be of the correct type.
   *
   * @param spec the specification to construct a key for, not null
   * @return a structured key for the structure handled by the value spec.
   */
  protected abstract K getStructuredKey(ValueSpecification spec);
}
