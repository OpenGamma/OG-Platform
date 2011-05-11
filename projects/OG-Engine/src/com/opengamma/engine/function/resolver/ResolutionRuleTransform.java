/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;

import com.opengamma.util.PublicAPI;

/**
 * A resolution rule transform allows a set of resolution rules to be altered based on the requirements of
 * a calculation configuration. For example, some rules might be suppressed for all or some targets, the
 * priorities changed, or additional rules added.
 */
@PublicAPI
public interface ResolutionRuleTransform {

  /**
   * Applies the transformation to a set of resolution rules.
   * 
   * @param rules the collection of rules to transform, not {@code null}. The supplied collection may be
   *        immutable - an implementation should manipulate a copy or a new collection if changes are
   *        required.
   * @return an updated collection, not {@code null}, or the original collection if no changes are required
   */
  Collection<ResolutionRule> transform(Collection<ResolutionRule> rules);

}
