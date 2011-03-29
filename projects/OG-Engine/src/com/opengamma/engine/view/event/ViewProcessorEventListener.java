/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.event;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueIdentifier;

/**
 * Allows implementers to provide callback methods that will be executed in response to events on a
 * {@link ViewProcessor}.
 */
public interface ViewProcessorEventListener {
  
  /**
   * Called immediately after a view process has been created.
   * 
   * @param viewProcessId  the unique identifier of the view process that has been created
   */
  void notifyViewProcessAdded(UniqueIdentifier viewProcessId);
  
  /**
   * Called immediately after a view process has been removed
   * 
   * @param viewProcessId  the unique identifier of the view process that has been removed
   */
  void notifyViewProcessRemoved(UniqueIdentifier viewProcessId);
  
  /**
   * Called immediately after a view processor starts up
   */
  void notifyViewProcessorStarted();
  
  /**
   * Called immediately after a view processor stops
   */
  void notifyViewProcessorStopped();

}
