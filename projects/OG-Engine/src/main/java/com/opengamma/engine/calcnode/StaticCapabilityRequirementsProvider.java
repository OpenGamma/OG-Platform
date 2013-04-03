/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;

/**
 * Always returns a static set of capabilities regardless of the job spec. 
 */
public class StaticCapabilityRequirementsProvider implements CapabilityRequirementsProvider {

  private CapabilityRequirements _currentRequirements = new CapabilityRequirements();

  @Override
  public CapabilityRequirements getCapabilityRequirements(final CalculationJob job) {
    return _currentRequirements;
  }

  public void addCapabilityRequirement(final Capability capability) {
    final CapabilityRequirements newRequirements = _currentRequirements.clone();
    newRequirements.requireCapability(capability);
    _currentRequirements = newRequirements;
  }

  public void addCapabilityRequirements(final Collection<Capability> capabilities) {
    final CapabilityRequirements newRequirements = _currentRequirements.clone();
    newRequirements.requireCapabilities(capabilities);
    _currentRequirements = newRequirements;
  }

  public void setCapabilityRequirements(final Collection<Capability> capabilities) {
    final CapabilityRequirements newRequirements = new CapabilityRequirements();
    newRequirements.requireCapabilities(capabilities);
    _currentRequirements = newRequirements;
  }

}
