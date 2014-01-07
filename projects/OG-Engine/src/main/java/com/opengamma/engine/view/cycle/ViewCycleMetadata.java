/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicAPI;

/**
 * Contains metadata about a {@link ViewCycle}.
 */
@PublicAPI
public interface ViewCycleMetadata {
  
  /**
   * Gets the unique identifier of the view cycle.
   * 
   * @return the unique identifier of the view cycle, not null
   */
  UniqueId getViewCycleId();
  
  /**
   * Gets the calculation configuration names.
   * 
   * @return the calculation configuration names, not null
   */
  Collection<String> getAllCalculationConfigurationNames();

  /**
   * Gets the computation targets for a given calculation configuration.
   * 
   * @param configurationName  the calculation configuration name, not null 
   * @return the computation targets for the given calculation configuration name, null if not found
   */
  Collection<ComputationTargetSpecification> getComputationTargets(String configurationName);

  /**
   * Gets the terminal outputs for a given calculation configuration.
   * 
   * @param configurationName  the calculation configuration name, not null
   * @return the terminal outputs for the given calculation configuration name, null if not found
   */
  Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs(String configurationName);

  /**
   * Gets the unique identifier associated with the market data snapshot used in the cycle.
   * 
   * @return the unique identifier of the market data snapshot
   */
  UniqueId getMarketDataSnapshotId();

  /**
   * Gets the valuation time used in the cycle.
   *  
   * @return the valuation time, not null
   */
  Instant getValuationTime();

  /**
   * Gets the version-correction used in the cycle.
   * 
   * @return the version-correction, not null
   */
  VersionCorrection getVersionCorrection();

  /**
   * Gets the unique identifier of the view definition referenced by the cycle.
   * 
   * @return the unique identifier of the view definition, not null
   */
  UniqueId getViewDefinitionId();
  
  /**
   * Gets the name of the view cycle, if one is defined
   * 
   * @return the name of the view cycle. Can be null.
   */
  String getName();
  
}
