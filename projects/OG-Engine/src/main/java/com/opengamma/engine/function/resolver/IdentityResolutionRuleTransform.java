/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;

/**
 * Resolution rule transform that trivially leaves the input rule set unchanged.
 */
public final class IdentityResolutionRuleTransform implements ResolutionRuleTransform {

  /**
   * Standard single instance, not managed as a singleton.
   */
  public static final IdentityResolutionRuleTransform INSTANCE = new IdentityResolutionRuleTransform();

  /**
   * Creates an instance.
   * Use the static constant where possible.
   */
  public IdentityResolutionRuleTransform() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<ResolutionRule> transform(final Collection<ResolutionRule> rules) {
    return rules;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object other) {
    return other instanceof IdentityResolutionRuleTransform;
  }

  @Override
  public int hashCode() {
    return IdentityResolutionRuleTransform.class.hashCode();
  }

}
