/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Suppression of resolution failure reporting.
 */
/* package */final class NullResolutionFailure extends ResolutionFailure {

  public static final NullResolutionFailure INSTANCE = new NullResolutionFailure();

  private NullResolutionFailure() {
  }

  @Override
  protected ResolutionFailure additionalRequirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return this;
  }

  @Override
  protected ResolutionFailure requirement(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    return this;
  }

  @Override
  protected ResolutionFailure requirements(final Map<ValueSpecification, ValueRequirement> available) {
    return this;
  }

  @Override
  protected ResolutionFailure getResultsFailed() {
    return this;
  }

  @Override
  protected ResolutionFailure getAdditionalRequirementsFailed() {
    return this;
  }

  @Override
  protected ResolutionFailure lateResolutionFailure() {
    return this;
  }

  @Override
  protected ResolutionFailure getRequirementsFailed() {
    return this;
  }

  @Override
  protected ResolutionFailure suppressed() {
    return this;
  }

  @Override
  protected ResolutionFailure checkFailure(final ValueRequirement valueRequirement) {
    return ResolutionFailureImpl.couldNotResolve(valueRequirement);
  }

  @Override
  public ValueRequirement getValueRequirement() {
    return null;
  }

  @Override
  public <T> Collection<T> accept(final ResolutionFailureVisitor<T> visitor) {
    return null;
  }

  @Override
  protected void merge(final ResolutionFailure failure) {
    // No-op
  }

  @Override
  public String toString() {
    return "NullResolutionFailure";
  }

  @Override
  public Object clone() {
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this;
  }

  @Override
  public int hashCode() {
    return 0;
  }

}
