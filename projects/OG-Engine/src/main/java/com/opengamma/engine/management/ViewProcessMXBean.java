/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MXBean;
import javax.management.openmbean.TabularData;

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
   * @return a table of successful restults by Security Type then Value Requirement, Properties
   */
  TabularData getResultsBySecurityType();

  /**
   * @return a table of successful restults by Value Requirement, Properties
   */
  TabularData getResultsByColumnRequirement();

  /**
   * @return overall number of successful calculations
   */
  int getSuccesses();

  /**
   * @return overall number of failed calculations
   */
  int getFailures();

  /**
   * @return total number of calculations expected
   */
  int getTotal();
  
  /**
   * method to process last cycles results and distill into tablular and stats results above.  On demand because it's not that lightweight.
   * @return true if successfully updated results
   */
  boolean processResults();

  /**
   * @return percentage of results that calculated successfully
   */
  double getPercentage();

  /**
   * @return true if 100% of results calculated successfully
   */
  boolean isCleanView100();

  /**
   * @return true if 99% of results calculated successfully
   */
  boolean isCleanView99();

  /**
   * @return true if 95% of results calculated successfully
   */
  boolean isCleanView95();
 
}
