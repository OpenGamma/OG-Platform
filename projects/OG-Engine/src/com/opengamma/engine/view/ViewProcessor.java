/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * A view processor manages and shares access to a set of view processes, which are each a computation job for a
 * particular {@link ViewDefinition}. Access to the results of a job is provided through {@link ViewClient}s, which are
 * also managed by the view processor.
 * <p>
 * There may be multiple view processes for a single {@link ViewDefinition}, each with possibly different execution
 * configurations. It may be preferable to share the results of an existing process, if one already exists, or to
 * execute a new one.
 */
@PublicAPI
public interface ViewProcessor extends UniqueIdentifiable {
  
  /**
   * Gets the unique identifier of the view processor
   * 
   * @return the identifier, not null
   */
  UniqueIdentifier getUniqueId();
  
  /** 
   * Gets this view processor's view definition repository containing the {@link ViewDefinition}s available for
   * computation.
   * 
   * @return the view definition repository, not null
   */
  ViewDefinitionRepository getViewDefinitionRepository();
  
  //-------------------------------------------------------------------------
  /**
   * Gets a collection of the view processes currently being managed by this view processor. A view process could be in
   * any state, and might have finished producing new results. 
   * 
   * @return a collection of the current view processes, not null
   */
  Collection<? extends ViewProcess> getViewProcesses();
  
  /**
   * Gets a view process by unique identifier.
   * 
   * @param viewProcessId  the unique identifier, not null
   * @return the view process, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no view process with that unique identifier
   */
  ViewProcess getViewProcess(UniqueIdentifier viewProcessId);

  //-------------------------------------------------------------------------
  /**
   * Creates a {@link ViewClient}
   * 
   * @param clientUser  the user of the client, not null
   * @return a new view client, not null
   */
  ViewClient createViewClient(UserPrincipal clientUser);
  
  /**
   * Gets a {@link ViewClient} by unique identifier.
   * 
   * @param clientId  the unique identifier, not null
   * @return the view client, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no view with that unique identifier
   */
  ViewClient getViewClient(UniqueIdentifier clientId);
  
}
