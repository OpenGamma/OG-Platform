/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Specifies the capability requirements of a job. It will only be passed to invokers that can
 * satisfy these requirements.
 */
public class CapabilityRequirements {

  private final Set<Capability> _requiredCapabilities = new HashSet<Capability>();

  public CapabilityRequirements() {
  }

  protected CapabilityRequirements(final CapabilityRequirements clone) {
    _requiredCapabilities.addAll(clone._requiredCapabilities);
  }

  public void requireCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    _requiredCapabilities.add(capability);
  }

  public void requireCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    _requiredCapabilities.addAll(capabilities);
  }

  protected Set<Capability> getRequiredCapabilities() {
    return _requiredCapabilities;
  }

  public CapabilityRequirements clone() {
    return new CapabilityRequirements(this);
  }

  public boolean satisfiedBy(final Collection<Capability> capabilities) {
    // TODO [ENG-42] Match up our requirements with the exported capabilities of the invoker
    return true;
  }

}
