/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Set;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * A client for managing a processor of views.
 * <p>
 * This manages multiple {@link ViewClient views} keyed by name.
 */
public interface ViewProcessorClient {

  /**
   * Checks if this processor is capable of supporting live computation of views.
   * @return true if live computation is supported
   */
  boolean isLiveComputationSupported();

  /**
   * Checks if this processor allows one-off view computation jobs to be provided.
   * @return true if one-off views are supported
   */
  boolean isOneOffComputationSupported();

  /**
   * Gets the list of all available view names.
   * @return the list of view names, not null
   */
  Set<String> getAvailableViewNames();

  /**
   * Gets the list of all live computing view names.
   * @return the list of view names, not null
   */
  Set<String> getLiveComputingViewNames();

  /**
   * Gets a single view by name.
   * @param viewName the name of the view to load
   * @return the view, null if not found
   */
  ViewClient getView(String viewName);

  /**
   * Starts live calculation of a specific view.
   * @param viewName  the view to start calculating, not null
   */
  void startLiveCalculation(String viewName);

  /**
   * Stops live calculation of a specific view.
   * @param viewName  the view to stop calculating, not null
   */
  void stopLiveCalculation(String viewName);

  /**
   * Gets the definition for a specific view.
   * @param viewName  the view to get the definition for, not null
   * @return the definition, not null
   */
  // REVIEW jim 26-Apr-2010 -- should really allow access to view definition repository.
  ViewDefinition getViewDefinition(String viewName);
  
  /**
   * Gets the user under which this clients operates.
   */
  UserPrincipal getUser();

}
