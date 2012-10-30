/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;

import com.opengamma.util.PublicAPI;

/**
 * Transforms one set of resolution rules into another.
 * <p>
 * This allows a set of resolution rules to be altered based on the requirements of a calculation configuration.
 * For example, some rules might be suppressed for all or some targets, the priorities may be changed,
 * or additional rules added.
 */
@PublicAPI
public interface ResolutionRuleTransform {

  /**
   * Transforms the input rules, returning a new collection of rules.
   * <p>
   * Implementations must be aware that the input may be immutable, and create a new collection
   * if performing modification.
   * If no modification is required, the input collection should be returned.
   * 
   * @param inputRules  the input collection of rules to transform, may be immutable, not null
   * @return the new collection of rules, or the input if no changes are required, not null
   */
  Collection<ResolutionRule> transform(Collection<ResolutionRule> inputRules);

}
