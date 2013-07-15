/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import org.threeten.bp.Instant;

/**
 * Storage for function costs.
 * <p>
 * This provides storage for function costs and is usually persistent.
 * The data is historic, such that the costs can be retrieved as seen in the past.
 */
public interface FunctionCostsMaster {

  /**
   * Loads a snapshot of function costs.
   * <p>
   * This will load the last stored version of the function costs before the specified
   * instant, where null means the current latest version.
   * 
   * @param configurationName  the configuration key, not null
   * @param functionId  the function id, not null
   * @param versionAsOf  the optional instant to retrieve data as of, null means latest
   * @return the function costs, not null
   */
  FunctionCostsDocument load(String configurationName, String functionId, Instant versionAsOf);

  /**
   * Stores a snapshot of function costs.
   * <p>
   * All fields except version must be set on input.
   * The version will be set to the current instant by this call.
   * 
   * @param costs  the function costs to store, not null
   * @return the updated input function costs, not null
   */
  FunctionCostsDocument store(final FunctionCostsDocument costs);

}
