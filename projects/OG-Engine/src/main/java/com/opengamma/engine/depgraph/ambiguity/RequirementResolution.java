/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Representation of a single resolution to a requirement. If ambiguities exist in a view definition or the resolution rules then there may be multiple resolutions possible. These can be held in a
 * {@link FullRequirementResolution} instance.
 */
public final class RequirementResolution {

  // TODO: Any changes to this structure need to be reflected in the Fudge builder

  /**
   * The resolved value specification.
   */
  private final ValueSpecification _specification;

  /**
   * The selected function. Note that the function identifier property in the value specification may not match that of the function in the repository.
   */
  private final DependencyNodeFunction _function;

  /**
   * The resolution(s) of any inputs to the function. If there is ambiguity such that alternative resolutions are possible, then additional instances of {@link RequirementResolution} will be created
   * that refer to these same inputs and referenced from the containing {@link FullRequirementResolution}.
   */
  private final Collection<FullRequirementResolution> _inputs;

  /**
   * The cached hash code.
   */
  private final int _hashCode;

  public RequirementResolution(final ValueSpecification specification, final DependencyNodeFunction function, final Collection<FullRequirementResolution> inputs) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(inputs, "inputs");
    _specification = specification;
    _function = function;
    _inputs = ImmutableSet.copyOf(inputs);
    int hc = getClass().hashCode();
    hc = (hc * 31) + _specification.hashCode();
    hc = (hc * 31) + DependencyNodeFunctionImpl.HASHING_STRATEGY.hashCode(_function);
    hc = (hc * 31) + _inputs.hashCode();
    _hashCode = hc;
  }

  public ValueSpecification getSpecification() {
    return _specification;
  }

  public DependencyNodeFunction getFunction() {
    return _function;
  }

  public Collection<FullRequirementResolution> getInputs() {
    return _inputs;
  }

  /**
   * Tests whether the given resolution is present in any of the inputs to this resolution. This prevents recursive structures from being constructed.
   */
  /* package */boolean contains(final FullRequirementResolution parent) {
    for (FullRequirementResolution input : _inputs) {
      if ((input == parent) || input.contains(parent)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether any of the inputs to this resolution contain any ambiguity.
   */
  /* package */boolean isAmbiguous() {
    for (FullRequirementResolution input : _inputs) {
      if (input.isDeeplyAmbiguous()) {
        return true;
      }
    }
    return false;
  }

  // Object

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof RequirementResolution)) {
      return false;
    }
    final RequirementResolution other = (RequirementResolution) o;
    return (hashCode() == other.hashCode()) && getSpecification().equals(other.getSpecification()) && DependencyNodeFunctionImpl.HASHING_STRATEGY.equals(getFunction(), other.getFunction()) &&
        getInputs().equals(other.getInputs());
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    return _specification.toString();
  }

}
