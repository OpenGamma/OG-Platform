/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicAPI;

/**
 * A base interface for the common structures for whole and delta result models. The result
 * model can be queried by target (giving a configuration/value space) or by configuration
 * (giving a target/value space).
 */
@PublicAPI
public interface ViewResultModel {

  /**
   * Gets the unique identifier of the view process responsible for the results.
   * 
   * @return the unique identifier, not null
   */
  UniqueId getViewProcessId();
  
  /**
   * Gets the unique identifier of the view cycle responsible for the results.
   * 
   * @return the unique identifier, not null
   */
  UniqueId getViewCycleId();
  
  /**
   * Gets the fully-resolved execution options from the view cycle responsible for the results.
   * 
   * @return the fully-resolved execution options, not null
   */
  ViewCycleExecutionOptions getViewCycleExecutionOptions();

  /**
   * Returns the time at which these results became available.
   * 
   * @return the time at which the results were posted
   */
  Instant getCalculationTime();
  
  /**
   * Gets the time taken to perform the calculation.
   * 
   * @return the time taken to perform the calculation, not null
   */
  Duration getCalculationDuration();
  
  /**
   * Gets the fully-resolved version-correction for which the results were calculated.
   * 
   * @return the fully-resolved version-correction, not null
   */
  VersionCorrection getVersionCorrection();

  /**
   * Returns all of the target specifications for the terminal outputs.
   * 
   * @return the target specifications
   */
  // REVIEW kirk 2009-12-31 -- This is intended to cross network boundaries,
  // so has to be at the level of specifications.
  Set<ComputationTargetSpecification> getAllTargets();

  /**
   * Returns all of the calculation configuration names.
   * 
   * @return the calculation configuration names
   */
  Collection<String> getCalculationConfigurationNames();

  /**
   * Returns the calculation result for a given configuration.
   * 
   * @param calcConfigurationName name of the configuration, not null
   * @return the calculation results, null if the configuration was not found
   */
  ViewCalculationResultModel getCalculationResult(String calcConfigurationName);

  /**
   * Returns the calculation result for all configurations for a given target.
   * 
   * @param targetSpecification the target to query, not null
   * @return the calculation results, null if the target was not found
   */
  ViewTargetResultModel getTargetResult(ComputationTargetSpecification targetSpecification);
  
  /**
   * Returns an iterator for iterating over all result entries. 
   * 
   * @return an iterator for iterating over all result entries
   */
  List<ViewResultEntry> getAllResults();
  
  /**
   * Returns union of value names across all results.
   * See {@link com.opengamma.engine.value.ValueSpecification#getValueName()}.
   * 
   * @return union of value names across all results
   */
  Set<String> getAllOutputValueNames();

}
