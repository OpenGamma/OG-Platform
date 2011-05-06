/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Provides access to a snapshot of the basic state involved in the computation of
 * an individual view calculation configuration. 
 */
public interface CompiledViewCalculationConfiguration {

  // TODO [PLAT-883] Currently no dependency graph browsing is possible through the interface, and therefore remotely.

  /**
   * Gets the name of the view calculation configuration.
   * 
   * @return the name of the view calculation configuration, not {@code null}
   */
  String getName();

  /**
   * Gets the set of terminal output {@link ValueSpecification} for the calculation configuration.
   * 
   * @return the set of terminal output {@link ValueSpecification} for the calculation configuration, not null
   */
  Set<ValueSpecification> getTerminalOutputSpecifications();

  /**
   * Gets the set of terminal output values for the calculation configuration.
   * 
   * @return the set of terminal output values, not null
   */
  Set<Pair<String, ValueProperties>> getTerminalOutputValues();

  /**
   * Gets the computation targets for the calculation configuration.
   * 
   * @return the set of computation targets, not null
   */
  Set<ComputationTarget> getComputationTargets();

  /**
   * Gets the live data requirements of the calculation configuration.
   * 
   * @return a map from each stated value requirement to the resolved value specification for live data, not null  
   */
  Map<ValueRequirement, ValueSpecification> getLiveDataRequirements();

}
