/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.event;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueId;

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
  void notifyViewProcessAdded(UniqueId viewProcessId);

  /**
   * Called immediately after a view process has been created.
   *
   * @param viewProcessId  the unique identifier of the view process that has been created
   * @param autoStartName the name of the configuration which was auto-started
   */
  void notifyViewAutomaticallyStarted(UniqueId viewProcessId, String autoStartName);
  
  /**
   * Called immediately after a view process has been removed
   * 
   * @param viewProcessId  the unique identifier of the view process that has been removed
   */
  void notifyViewProcessRemoved(UniqueId viewProcessId);
  
  /**
   * Called immediately after a view client has been created.
   * 
   * @param viewClientId  the unique identifier of the view client that has been created
   */
  void notifyViewClientAdded(UniqueId viewClientId);
  
  /**
   * Called immediately after a view client has been removed.
   * 
   * @param viewClientId  the unique identifier of the view client that has been removed
   */
  void notifyViewClientRemoved(UniqueId viewClientId);
  
  /**
   * Called immediately after a view processor starts up
   */
  void notifyViewProcessorStarted();
  
  /**
   * Called immediately after a view processor stops
   */
  void notifyViewProcessorStopped();

}
