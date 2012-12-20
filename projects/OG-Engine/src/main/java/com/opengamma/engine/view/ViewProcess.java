/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A view process represents a computation job for a {@link ViewDefinition}; that is, a sequence of one or more
 * computation cycles. Each cycle evaluates the view definition at a particular time. A job may run forever, for
 * example for real-time evaluation of the view definition, or it may run over a finite set of evaluation times, for
 * example in batch computations.
 * <p>
 * Results are obtained through a {@link ViewClient}.
 */
@PublicAPI
public interface ViewProcess extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the identifier, not null
   */
  UniqueId getUniqueId();
  
  /**
   * Gets the id of the underlying view definition
   * 
   * @return the id of the underlying view definition
   */
  UniqueId getDefinitionId();
  
  /**
   * A convenience method for obtaining the latest view definition. This is not necessarily the version in use by any
   * current computation.
   * 
   * @return the underlying view definition, not null
   */
  
  ViewDefinition getLatestViewDefinition();
  
  /**
   * Gets the state of the view process.
   * 
   * @return the computation state of the view process, not null
   */
  ViewProcessState getState();
  
  /**
   * Gets the live data injector for overriding arbitrary live data for the view process.
   * 
   * @return the live data injector, not null
   */
  MarketDataInjector getLiveDataOverrideInjector();
  
  /**
   * Terminates this view process, detaching any clients from it.
   */
  void shutdown();
 
}
