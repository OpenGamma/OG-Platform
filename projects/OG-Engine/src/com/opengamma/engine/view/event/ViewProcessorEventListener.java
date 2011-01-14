/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.event;



/**
 * Allows implementers to register callback methods that will be executed when a
 * {@link com.opengamma.engine.view.View} is created and removed
 *
 */
public interface ViewProcessorEventListener {
  
  /**
   * Called immediately after a view has been created.
   * 
   * @param viewName the name of the View the operation relates to
   */
  void notifyViewAdded(String viewName);
  
  /**
   * Called immediately after a view has been removed
   * 
   * @param viewName the name of the View the operation relates to
   */
  void notifyViewRemoved(String viewName);
  
  /**
   * Called immediately after a view processor starts up
   * 
   */
  void notifyViewProcessorStarted();
  
  /**
   * Called immediately after a view processor stops
   */
  void notifyViewProcessorStopped();

}
