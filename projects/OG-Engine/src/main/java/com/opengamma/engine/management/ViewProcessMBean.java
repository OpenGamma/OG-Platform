/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.id.UniqueId;

/**
 * A management bean for a View
 * @deprecated use ViewProcessMXBean
 */
@Deprecated
public interface ViewProcessMBean {

  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the identifier, not null
   */
  UniqueId getUniqueId();
  
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
  UniqueId getDefinitionId();
  
  /**
   * Gets the state of the view process.
   * 
   * @return the computation state of the view process, not null
   */
  ViewProcessState getState();
  
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
  
}
