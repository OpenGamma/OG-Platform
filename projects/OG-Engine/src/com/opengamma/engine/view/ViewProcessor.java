/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
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
  UniqueId getUniqueId();
  
  /** 
   * Gets this view processor's view definition repository containing the {@link ViewDefinition}s available for
   * computation.
   * 
   * @return the view definition repository, not null
   */
  ViewDefinitionRepository getViewDefinitionRepository();
  
  //------------------------------------------------------------------------- 
  /**
   * Gets a view process by unique identifier.
   * 
   * @param viewProcessId  the unique identifier, not null
   * @return the view process, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no view process with that unique identifier
   */
  ViewProcess getViewProcess(UniqueId viewProcessId);

  /** 
   * Gets this view processor's live market data source registry containing the DataSource available for
   * use with {@link LiveMarketDataSpecification}.
   * 
   * @return the view definition repository, not null
   */
  LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry();
  
  
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
  ViewClient getViewClient(UniqueId clientId);
  
  //-------------------------------------------------------------------------
  /**
   * Gets the resource manager for view cycles.
   * 
   * @return the resource manager for view cycles, not null
   */
  EngineResourceManager<? extends ViewCycle> getViewCycleManager();
  
}
