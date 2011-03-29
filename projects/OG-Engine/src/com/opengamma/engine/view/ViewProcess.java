/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicAPI;

/**
 * A view process represents a computation job for a particular {@link ViewDefinition}; that is, a sequence of one or
 * more computation cycles. Each cycle evaluates the view definition at a particular time. A job may run forever, for
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
  UniqueIdentifier getUniqueId();
  
  /**
   * Indicates whether the view process was created for a batch computation.
   * <p>
   * This may affect the behaviour of any attached clients.
   * 
   * @return {@code true} if the process was created for a batch computation, {@code false} otherwise
   */
  boolean isBatchProcess();
  
  /**
   * Gets the name of the underlying view definition
   * 
   * @return the name of the underlying view definition
   */
  String getDefinitionName();
  
  /**
   * Gets the underlying view definition
   * 
   * @return the underlying view definition
   */
  ViewDefinition getDefinition();
  
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
  LiveDataInjector getLiveDataOverrideInjector();
  
  /**
   * Terminates this view process, detaching any clients from it.
   */
  void shutdown();
 
}
