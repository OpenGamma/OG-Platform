/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * A view represents a {@link ViewDefinition} in the context of a {@link ViewProcessor}; this is everything required
 * to perform computations and create clients which listen to the output. The view is responsible for managing its own
 * computation efficiently in response to demand from its clients, and for sharing computation results among its
 * clients.
 * <p>
 * Views must be initialized by calling {@link #init()} before computations can be performed. This operation involves
 * resolving every aspect of the view definition and building the view's dependency graph, which is potentially
 * time-consuming. Initialization only takes place on the first call to {@link #init()}; therefore it should be called
 * defensively without performance considerations.
 * <p>
 * Uninitialized views may be used only to access the underlying view definition, and parts of the processing context
 * such as live data overrides. An {@link IllegalStateException} is thrown when attempting to perform an operation that
 * is unsupported on an uninitialized view. 
 */
@PublicAPI
public interface View {

  /**
   * Gets the name of the underlying view definition
   * <p>
   * May be used on uninitialized views.
   * 
   * @return the name of the underlying view definition
   */
  String getName();
  
  /**
   * Gets the underlying view definition
   * <p>
   * May be used on uninitialized views.
   * 
   * @return the underlying view definition
   */
  ViewDefinition getDefinition();
  
  /**
   * Synchronously initializes the view. Until a view is initialized, it can be used only to access underlying
   * metadata, such as the view definition. If the view has already been initialized, this method does nothing and
   * returns immediately. 
   * <p>
   * Initialization involves compiling the view definition into dependency graphs, which could be a lengthy process.
   * After initialization, the view is ready to be executed.
   * <p>
   * If initialization fails, an exception is thrown but the view remains in a consistent state from which
   * initialization may be re-attempted.
   */
  void init();
  
  /**
   * Gets the fully-resolved reference portfolio for the underlying view definition. 
   * 
   * @return the fully-resolved reference portfolio
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  Portfolio getPortfolio();
  
  /**
   * Creates a client which backs onto the view.
   * 
   * @param credentials  the user for whom the client is being created. This user must have sufficient permissions on
   *                     the view. 
   * @return a new {@link ViewClient} instance
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  ViewClient createClient(UserPrincipal credentials);
  
  /**
   * Retrieves an existing client by identifier.
   * <p>
   * May be used on uninitialized views, but will always return null since no clients can exist.
   * 
   * @param id  the identifier of an existing client
   * @return the existing client, or <code>null</code> if no such client exists.
   */
  ViewClient getClient(UniqueIdentifier id);
    
  /**
   * Checks that the given user has access to the live data inputs required for computation of this view.
   * 
   * @param user  the user
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  void assertAccessToLiveDataRequirements(UserPrincipal user);
  
  /**
   * Gets the result of the latest calculation, if applicable.
   * <p>
   * May be used on uninitialized views.
   * 
   * @return the result of the latest calculation, or <code>null</code> if no calculation has yet completed.
   */
  ViewComputationResultModel getLatestResult();
  
  /**
   * Gets the live data required for computation of the view
   * 
   * @return a set of value requirements describing the live data required for computation of the view
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  Set<ValueRequirement> getRequiredLiveData();
  
  /**
   * Gets a set of all security types present in the view's dependency graph; that is, all security types on which
   * calculations must be performed.
   * 
   * @return a set of all security types in the view's dependency graph, not null
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  Set<String> getAllSecurityTypes();
  
  /**
   * Synchronously runs a single computation cycle using live data.
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  void runOneCycle();
  
  /**
   * Synchronously runs a single computation cycle using data snapshotted at the given time. This cannot be used while
   * live computation is running.
   * 
   * @param valuationTime  the time of an existing snapshot of live data, which should be used during the computation
   *                       cycle
   *                       
   * @throws IllegalStateException  if the view has not been initialized
   */
  void runOneCycle(long valuationTime);
  
  /**
   * Indicates whether the view is computing live results automatically. A view in this state will perform a
   * computation cycle whenever changes to its inputs have occurred since the last computation cycle.
   * <p>
   * May be used on uninitialized views.
   * 
   * @return <code>true</code> if the view has been started, <code>false</code> otherwise
   */
  boolean isLiveComputationRunning();
  
  /**
   * Gets the live data injector for overriding arbitrary live data for the view.
   * <p>
   * May be used on uninitialized views.
   * 
   * @return the live data injector, not null
   */
  LiveDataInjector getLiveDataOverrideInjector();
  
}
