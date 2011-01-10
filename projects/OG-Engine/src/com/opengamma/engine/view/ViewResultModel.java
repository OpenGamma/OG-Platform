/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.List;

import javax.time.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.PublicAPI;

/**
 * A base interface for the common structures for whole and delta result models. The result
 * model can be queried by target (giving a configuration/value space) or by configuration
 * (giving a target/value space).
 */
@PublicAPI
public interface ViewResultModel {

  /**
   * Returns the name of the view this contains results for.
   * 
   * @return the view name
   */
  String getViewName();

  /**
   * Returns the snapshot time at which live data values were taken for this result.
   * 
   * @return the snapshot time
   */
  Instant getValuationTime();

  /**
   * Returns the time at which these results became available.
   * 
   * @return the time at which the results were posted
   */
  Instant getResultTimestamp();

  /**
   * Returns all of the target specifications for the terminal outputs.
   * 
   * @return the target specifications
   */
  // REVIEW kirk 2009-12-31 -- This is intended to cross network boundaries,
  // so has to be at the level of specifications.
  Collection<ComputationTargetSpecification> getAllTargets();

  /**
   * Returns all of the calculation configuration names.
   * 
   * @return the calculation configuration names
   */
  Collection<String> getCalculationConfigurationNames();

  /**
   * Returns the calculation result for a given configuration.
   * 
   * @param calcConfigurationName name of the configuration, not {@code null}
   * @return the calculation results, or {@code null} if the configuration was not found
   */
  ViewCalculationResultModel getCalculationResult(String calcConfigurationName);

  /**
   * Returns the calculation result for all configurations for a given target.
   * 
   * @param targetSpecification the target to query, not {@code null}
   * @return the calculation results, or {@code null} if the target was not found
   */
  ViewTargetResultModel getTargetResult(ComputationTargetSpecification targetSpecification);
  
  /**
   * Returns an iterator for iterating over all result entries. 
   * 
   * @return an iterator for iterating over all result entries
   */
  List<ViewResultEntry> getAllResults();

}
