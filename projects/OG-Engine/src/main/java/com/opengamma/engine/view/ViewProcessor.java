/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
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
public interface ViewProcessor {

  /**
   * Gets the the name of the view processor.
   * <p>
   * The name can be used to identify the view processor.
   * It should be unique, although this is not guaranteed.
   * 
   * @return the name, not null
   */
  String getName();

  /** 
   * Gets this view processor's config source containing the configuration available for
   * computation.
   * 
   * @return the config source, not null
   */
  ConfigSource getConfigSource();
  
  /** 
   * Gets this view processor's market data provider repository containing named, pre-built market data specifications
   * which are available for use by clients.
   * 
   * @return the view definition repository, not null
   * @deprecated this is only required for the legacy analytics UI
   */
  @Deprecated
  NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository();

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
