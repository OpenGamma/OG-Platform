/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Set;

/**
 * A management bean for a View
 *
 */
public interface ViewMBean {

  /**
   * Gets the name of the underlying view definition
   * <p>
   * May be used on uninitialized views.
   * 
   * @return the name of the underlying view definition
   */
  String getName();
  
  /**
   * Synchronously initializes the view. Until a view is initialized, it can be used only to access underlying
   * metadata, such as the view definition. If the view has already been initialized, this method does nothing and
   * returns immediately. 
   * <p>
   * Initialization involves compiling the view definition into dependency graphs, which could be a lengthy process.
   * After initialization, the view is ready to be executed.
   * <p>
   * If initialization fails, an exception is thrown but the view remains in a consistent state from which
   * initialization may be re-attempted.
   */
  void init();
  
  /**
   * Gets the portfolio name
   * 
   * @return the portfolio name
   */
  String getPortfolioName();
  
  /**
   * Gets the portfolio Identifier
   * 
   * @return the portfolio identifier
   */
  String getPortfolioIdentifier();
  
  /**
   * Gets a set of all security types present in the view's dependency graph; that is, all security types on which
   * calculations must be performed.
   * 
   * @return a set of all security types in the view's dependency graph, not null
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  Set<String> getAllSecurityTypes();
  
  /**
   * Gets the live data required for computation of the view
   * 
   * @return a set of value requirements describing the live data required for computation of the view
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  Set<String> getRequiredLiveData();
  
  /**
   * Indicates whether the view is computing live results automatically. A view in this state will perform a
   * computation cycle whenever changes to its inputs have occurred since the last computation cycle.
   * <p>
   * May be used on uninitialized views.
   * 
   * @return <code>true</code> if the view has been started, <code>false</code> otherwise
   */
  boolean isLiveComputationRunning();
  
  /**
   * Suspends all operations on the view, blocking until everything is in a suspendable state. While suspended,
   * any operations which would alter the state of the view will block until {@link #resume} is called.
   */
  void suspend();

  /**
   * Resumes operations on the view suspended by {@link #suspend}.
   */
  void resume();
  
}
