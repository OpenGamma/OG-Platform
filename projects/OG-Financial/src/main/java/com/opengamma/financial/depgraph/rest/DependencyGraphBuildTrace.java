/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.JodaBeanUtils;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Class to hold the data collected by the dependency graph builders for debugging.
 */
public class DependencyGraphBuildTrace {
  /**
   * The dependency graph that was constructed itself.
   */
  private DependencyGraph _depGraph;

  /**
   * A list of exceptions along with counts for them.
   */
  private Map<Throwable, Integer> _exceptionsWithCounts;

  /**
   * A list of resolution failures that occurred.
   */
  private List<ResolutionFailure> _failures;

  /**
   * A map showing how value requirements were resolved to specifications
   */
  private Map<ValueRequirement, ValueSpecification> _mappings;

  protected DependencyGraphBuildTrace(DependencyGraph depGraph,
      Map<Throwable, Integer> exceptionsWithCounts,
      List<ResolutionFailure> failures,
      Map<ValueRequirement, ValueSpecification> mappings) {
    _depGraph = depGraph;
    _exceptionsWithCounts = exceptionsWithCounts;
    _failures = failures;
    _mappings = mappings;
  }

  public static DependencyGraphBuildTrace of(DependencyGraph depGraph,
      Map<Throwable, Integer> exceptionsWithCounts,
      List<ResolutionFailure> failures,
      Map<ValueRequirement, ValueSpecification> mappings) {
    return new DependencyGraphBuildTrace(depGraph, exceptionsWithCounts, failures, mappings);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DependencyGraphBuildTrace other = (DependencyGraphBuildTrace) obj;
      return JodaBeanUtils.equal(getDependencyGraph(), other.getDependencyGraph()) &&
          JodaBeanUtils.equal(getExceptionsWithCounts(), other.getExceptionsWithCounts()) &&
          JodaBeanUtils.equal(getFailures(), other.getFailures()) &&
          JodaBeanUtils.equal(getMappings(), other.getMappings());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getDependencyGraph());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExceptionsWithCounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFailures());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMappings());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the dependency graph that was constructed itself.
   * @return the value of the property
   */
  public DependencyGraph getDependencyGraph() {
    return _depGraph;
  }

  /**
   * Sets the dependency graph that was constructed itself.
   * @param depGraph  the new value of the property
   */
  public void setDepGraph(DependencyGraph depGraph) {
    this._depGraph = depGraph;
  }

  /**
   * Gets a list of exceptions along with counts for them.
   * @return the value of the property
   */
  public Map<Throwable, Integer> getExceptionsWithCounts() {
    return _exceptionsWithCounts;
  }

  /**
   * Sets a list of exceptions along with counts for them.
   * @param exceptionCounts  the new value of the property
   */
  public void setExceptionsWithCounts(Map<Throwable, Integer> exceptionCounts) {
    this._exceptionsWithCounts = exceptionCounts;
  }

  /**
   * Gets a list of resolution failures that occurred.
   * @return the value of the property
   */
  public List<ResolutionFailure> getFailures() {
    return _failures;
  }

  /**
   * Sets a list of resolution failures that occurred.
   * @param failures  the new value of the property
   */
  public void setFailures(List<ResolutionFailure> failures) {
    this._failures = failures;
  }

  /**
   * Gets a map showing how value requirements were resolved to specifications
   * @return the value of the property
   */
  public Map<ValueRequirement, ValueSpecification> getMappings() {
    return _mappings;
  }

  /**
   * Sets a map showing how value requirements were resolved to specifications
   * @param mappings  the new value of the property
   */
  public void setMappings(Map<ValueRequirement, ValueSpecification> mappings) {
    this._mappings = mappings;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
