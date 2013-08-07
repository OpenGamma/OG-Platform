/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Responsible for extracting a structured identifier from a node in a
 * dependency graph to see if it should be proxied. If the node is not
 * supported or has already been returned then null is returned.
 *
 * @param <K> the underlying type of the structured identifier to be returned
 */
public abstract class NodeExtractor<K extends Serializable> {

  /**
   * The name (on the value specification) that must be matched for
   * extraction to go ahead.
   */
  private final String _specificationName;

  /**
   * Constructor, taking the value specification name that this extractor
   * will match on.
   * @param specificationName the name for the extractor to match on
   */
  public NodeExtractor(String specificationName) {
    _specificationName = specificationName;
  }

  protected static String getSingleProperty(final ValueSpecification spec, final String propertyName) {
    final ValueProperties properties = spec.getProperties();
    final Set<String> curves = properties.getValues(propertyName);
    return Iterables.getOnlyElement(curves);
  }

  protected static String getOptionalProperty(final ValueSpecification spec, final String propertyName) {
    final ValueProperties properties = spec.getProperties();
    final Set<String> curves = properties.getValues(propertyName);
    if (curves != null && !curves.isEmpty()) {
      return Iterables.getOnlyElement(curves);
    } else {
      return null;
    }
  }

  /**
   * Get the name (on the value specification) that must be matched for
   * extraction to go ahead.
   *
   * @return the value specification name to match on
   */
  public String getSpecificationName() {
    return _specificationName;
  }

  /**
   * Attempt to extract a structured identifier from the supplied node. An identifier
   * will be returned if the node matches the specification name and the node has
   * not already been proxied (or is a proxy itself).
   *
   * @param node the node to attempt to extract an identifier from, not null
   * @return a structured identifier for the node if it matches the criteria, null otherwise
   */
  public StructureIdentifier<K> getStructuredIdentifier(DependencyNode node) {

    for (ValueSpecification valueSpecification : node.getOutputValues()) {

      // Check that the node we're looking at is not a manipulator, nor that a manipulator
      // is already in place as a dependent node
      if (nodeMatchesRequirement(node, valueSpecification)) {
        return getStructuredIdentifier(valueSpecification);
      }
    }

    return null;
  }

  /**
   * Gets the structured key from the passed value specification. The specification will previously have
   * been matched against the configured specification name so can be assumed to be of the correct type.
   *
   * @param spec the specification to construct a key for, not null
   * @return a structured key for the structure handled by the value spec.
   */
  public abstract StructureIdentifier<K> getStructuredIdentifier(ValueSpecification spec);

  private boolean nodeMatchesRequirement(DependencyNode node, ValueSpecification valueSpecification) {

    // Check spec name matches what we want ...
    if (_specificationName.equals(valueSpecification.getValueName()) &&
        // but that it's not a manipulation node we've added already
        valueSpecification.getProperty("MANIPULATION_MODE") == null) {

      // nor that it already has a dependent manipulation node
      for (DependencyNode dependent : node.getDependentNodes()) {

        for (ValueSpecification outputValue : dependent.getOutputValues()) {

          if (_specificationName.equals(outputValue.getValueName()) && outputValue.getProperty("MANIPULATION_MODE") != null) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }
}
