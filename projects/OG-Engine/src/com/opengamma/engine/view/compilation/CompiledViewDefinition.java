/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;

/**
 * Provides access to a snapshot of the basic state required for computation of a view, valid for a period of valuation
 * times.
 */
public interface CompiledViewDefinition {
  
  /**
   * Gets the view definition which was compiled.
   * 
   * @return the view definition, not null
   */
  ViewDefinition getViewDefinition();
  
  /**
   * Gets the fully-resolved portfolio associated with the view definition.
   * 
   * @return the fully-resolved portfolio, or {@code null} if no portfolio is associated with the view definition
   */
  Portfolio getPortfolio();
  
  /**
   * Gets the live data requirements implied by the dependency graphs.
   * 
   * @return a map from each stated value requirement to the resolved value specification for live data, not null  
   */
  Map<ValueRequirement, ValueSpecification> getLiveDataRequirements();
  
  /**
   * Gets a set of every value requirement name across every calculation configuration. 
   * 
   * @return a set of all value requirement names, not null
   */
  Set<String> getOutputValueNames();
  
  /**
   * Gets a set of all computation targets across every calculation configuration.
   * 
   * @return a set of all computation targets, not null
   */
  Set<ComputationTarget> getComputationTargets();
  
  /**
   * Gets a set of the security types present in the dependency graphs; that is, all security types on which
   * calculations must be performed.
   * 
   * @return a set of all security types in the view's dependency graphs, not null
   */
  Set<String> getSecurityTypes();
  
  /**
   * Gets the instant from which the evaluation model is valid, inclusive.
   *  
   * @return the instant from which the evaluation model is valid, or {@code null} to indicate no restriction
   */
  Instant getValidFrom();
  
  /**
   * Gets the instant to which the evaluation model is valid, inclusive.
   * 
   * @return the instant to which the evaluation model is valid, or {@code null} to indicate no restriction 
   */
  Instant getValidTo();
  
}
