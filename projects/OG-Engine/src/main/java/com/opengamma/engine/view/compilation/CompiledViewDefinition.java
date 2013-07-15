/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * Provides access to a snapshot of the basic state required for computation of a view, valid for a period of valuation times.
 */
public interface CompiledViewDefinition {

  /**
   * Gets the resolver version/correction date that was used to retrieve the portfolio and any other data structures needed by functions used in the compilation.
   * 
   * @return the version/correction used to resolve any targets, including the portfolio
   */
  VersionCorrection getResolverVersionCorrection();

  /**
   * Gets the unique compilation identifier. Two compiled view definitions with the same unique compilation identifier will contain the same dependency graphs for exactly the same reasons - that is,
   * the compiled forms are identical except for the resolver version/correction timestamp.
   * 
   * @return a compilation identifier, unique within the scope of any caches that may persist or transport compiled view definitions around an installation
   */
  String getCompilationIdentifier();

  /**
   * Returns a copy of this object with an updated version/correction parameter.
   * 
   * @param resolverVersionCorrection the resolver version/correction date for the copy
   * @return the copy
   */
  CompiledViewDefinition withResolverVersionCorrection(VersionCorrection resolverVersionCorrection);

  /**
   * Gets the view definition which was compiled.
   *
   * @return the view definition, not null
   */
  ViewDefinition getViewDefinition();

  /**
   * Gets the fully-resolved portfolio associated with the view definition.
   *
   * @return the fully-resolved portfolio, or null if no portfolio is associated with the view definition
   */
  Portfolio getPortfolio();

  /**
   * Gets a compiled view calculation configuration.
   *
   * @param viewCalculationConfiguration the name of the calculation configuration, not null
   * @return the compiled view calculation configuration, or null if no calculation configuration exists with that name.
   */
  CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(String viewCalculationConfiguration);

  /**
   * Gets all compiled view calculation configurations.
   *
   * @return a collection of all compiled view calcualtion configurations, not null
   */
  Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations();

  /**
   * Gets all compiled view calculation configurations.
   *
   * @return a collection of all compiled view calcualtion configurations, not null
   */
  Map<String, CompiledViewCalculationConfiguration> getCompiledCalculationConfigurationsMap();

  /**
   * Gets the combined market data requirements of all calculation configurations. These specifications correspond to the values that must be sourced by the leaf nodes of the graph and may be used to
   * establish market data subscriptions. If the external identifiers corresponding to the market data lines are required, the associated market data provider that participated in the graph building
   * must be consulted to provide a conversion, or the aliased requirements from the individual calculation configurations may be used.
   *
   * @return the value specifications for all necessary market data, not null
   */
  Set<ValueSpecification> getMarketDataRequirements();

  /**
   * Gets the combined terminal values requirements along with resolved specifications.
   *
   * @return a map from the resolved value specification to each stated value requirement which the specification satisfies for all termianl values, not null
   */
  Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements();

  /**
   * Gets the combined computation targets across every calculation configuration.
   *
   * @return a set of all computation targets, not null
   */
  Set<ComputationTargetSpecification> getComputationTargets();

  /**
   * Gets the instant from which the compiled view definition is valid, inclusive.
   *
   * @return the instant from which the evaluation model is valid, or null to indicate no restriction
   */
  Instant getValidFrom();

  /**
   * Gets the instant to which the compiled view definition is valid, inclusive.
   *
   * @return the instant to which the evaluation model is valid, or null to indicate no restriction
   */
  Instant getValidTo();
}
