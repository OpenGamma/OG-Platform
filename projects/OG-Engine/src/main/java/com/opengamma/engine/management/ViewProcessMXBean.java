/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MXBean;

/**
 * A management bean for a View
 *
 */
@MXBean
public interface ViewProcessMXBean {

  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the identifier, not null
   */
  String getUniqueId();
  
  /**
   * Gets the portfolio Identifier
   * 
   * @return the portfolio identifier
   */
  String getPortfolioId();
  
  /**
   * Gets the name of the underlying view definition
   * 
   * @return the name of the underlying view definition
   */
  String getDefinitionId();
  
  /**
   * Gets the state of the view process.
   * 
   * @return the computation state of the view process, not null
   */
  String getState();
  
  /**
   * Gets whether the view process is persistent. 
   * 
   * @return true if the view process is persistent, false otherwise
   */
  boolean isPersistent();
  
  /**
   * Terminates this view process, detaching any clients from it.
   */
  void shutdown();
  
  /**
   * Suspends all operations on the view, blocking until everything is in a suspendable state. While suspended,
   * any operations which would alter the state of the view will block until {@link #resume} is called.
   */
  void suspend();

  /**
   * Resumes operations on the view suspended by {@link #suspend}.
   */
  void resume();

  /**
   * The result of the last cycle execution, or PENDING if cycle hasn't happened yet
   * @return description of the cycle state
   */
  String getLastComputeCycleState();

  /**
   * Compilation failed exception message, if applicable
   * @return exception message, or null if not applicable
   */
  String getCompilationFailedException();

  /**
   * Valuation time used during failed compilation
   * @return the valuation time or null if not applicable.
   */
  String getCompilationFailedValuationTime();

  /**
   * @return whether market data permissions were granted during compilation, or PENDING if not yet determined.
   */
  String getMarketDataPermissionsState();

  /**
   * The state of view compilation
   * @return PENDING, SUCCESSFUL, FAILED
   */
  String getCompilationState();

  /**
   * @return the name of the view
   */
  String getViewName();
  
  /**
   * method to process last cycles results and distill into tablular results. On demand because it's not that lightweight.
   * @return the statistics
   */
  ViewProcessStatsProcessor generateResultsModelStatistics();

  /**
   * @return when the last successful cycle was calculated, null if
   * there has not been a successful cycle
   */
  String getLastSuccessfulCycleTimeStamp();

  /**
   * @return how long the last successful cycle took to calculate, null if
   * there has not been a successful cycle
   */
  Long getLastSuccessfulCycleDuration();

  /**
   * @return how long since the last successful cycle ran, null if
   * there has not been a successful cycle
   */
  Long getTimeSinceLastSuccessfulCycle();
}
