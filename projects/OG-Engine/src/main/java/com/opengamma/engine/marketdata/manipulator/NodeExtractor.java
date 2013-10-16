/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.io.Serializable;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Responsible for extracting a structured identifier from a node in a dependency graph to see if it should be proxied. If the node is not supported or has already been returned then null is returned.
 * 
 * @param <K> the underlying type of the structured identifier to be returned
 */
public abstract class NodeExtractor<K extends Serializable> {

  /**
   * The name (on the value specification) that must be matched for extraction to go ahead.
   */
  private final String _specificationName;

  /**
   * Constructor, taking the value specification name that this extractor will match on.
   * 
   * @param specificationName the name for the extractor to match on
   */
  public NodeExtractor(String specificationName) {
    _specificationName = specificationName;
  }

  protected static String getProperty(final ValueSpecification spec, final String propertyName) {
    return spec.getProperties().getStrictValue(propertyName);
  }

  /**
   * Get the name (on the value specification) that must be matched for extraction to go ahead.
   * 
   * @return the value specification name to match on
   */
  public String getSpecificationName() {
    return _specificationName;
  }

  /**
   * Gets the structured key from the passed value specification. The specification will previously have been matched against the configured specification name so can be assumed to be of the correct
   * type.
   * 
   * @param spec the specification to construct a key for, not null
   * @return a structured key for the structure handled by the value spec.
   */
  public abstract StructureIdentifier<K> getStructuredIdentifier(ValueSpecification spec);

}
