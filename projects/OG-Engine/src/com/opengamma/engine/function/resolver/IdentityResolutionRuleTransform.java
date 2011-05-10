/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;

/**
 * Trivial implementation of {@link ResolutionRuleTransform} that leaves the input rule set unchanged.
 */
public final class IdentityResolutionRuleTransform implements ResolutionRuleTransform {

  /**
   * Default instance.
   */
  public static final ResolutionRuleTransform INSTANCE = new IdentityResolutionRuleTransform();

  @Override
  public Collection<ResolutionRule> transform(final Collection<ResolutionRule> rules) {
    return rules;
  }

  @Override
  public boolean equals(final Object other) {
    return (other == this) || (other instanceof IdentityResolutionRuleTransform);
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(INSTANCE);
  }

}
