/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.id.UniqueIdentifier;

/**
 * A management bean for a View
 *
 */
public interface ViewProcessMBean {

  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the identifier, not null
   */
  UniqueIdentifier getUniqueId();
  
  /**
   * Gets the portfolio Identifier
   * 
   * @return the portfolio identifier
   */
  String getPortfolioIdentifier();
  
  /**
   * Gets the name of the underlying view definition
   * 
   * @return the name of the underlying view definition
   */
  String getDefinitionName();
  
  /**
   * Gets the state of the view process.
   * 
   * @return the computation state of the view process, not null
   */
  ViewProcessState getState();
  
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
