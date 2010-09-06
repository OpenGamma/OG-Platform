/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.NoSuchElementException;
import java.util.Set;

import com.opengamma.livedata.msg.UserPrincipal;

/**
 * A view processor manages the computation of views, and is responsible for initializing the computation context of
 * these views. A view processor can manage only one instance of any view, but potentially there can be many view
 * processors responsible for possibly-overlapping sets of views.
 * <p>
 * This interface exposes full control over the view processor, for example using through
 * {@link #stopProcessing(String)}, and is designed for internal use by the engine. The {@link ViewProcessorClient}
 * interface is designed to provide more restricted access to a {@link ViewProcessor} for external use.
 */
public interface ViewProcessor {

  /**
   * Gets the names of the views which the view processor can provide access to. Not all of these views are necessarily
   * initialized, less so being processed.
   * 
   * @return  a set of view names
   */
  Set<String> getViewNames();
  
  /**
   * Obtain an already-initialized {@link View} instance.
   * <p/>
   * This method will only return a view if it has already been initialized and if the given user has access to the
   * view.
   * <p/>
   * If there is a view definition available, and the user has access to it, but this method returns {@code null}, the
   * view needs to be initialized using {@link #initializeView(String)}.
   * 
   * @param name  the name of the view to obtain, not null
   * @param credentials  the user attempting to access the view, not null
   * @return  the initialized view, or {@code null}.
   */
  View getView(String name, UserPrincipal credentials);
  
  /**
  * Initializes the named view.
  *
  * @param viewName the name of the view to initialize
  * @throws NoSuchElementException if a view with the name is not available
  */
  void initializeView(String viewName);
  
  /**
   * Attempts to get a view by name, initializing it if necessary.
   * 
   * @param viewName  the name of the view to obtain, not null
   * @param credentials  the user attempting to access the view, not null
   * @return the view, not null
   */
  View getOrInitializeView(String viewName, UserPrincipal credentials);
  
  /**
   * Causes processing to begin on the named view.
   * 
   * @param viewName  the name of the view
   */
  void startProcessing(String viewName);
  
  /**
   * Causes processing to stop on the named view.
   * 
   * @param viewName  the name of the view
   */
  void stopProcessing(String viewName);
  
}
