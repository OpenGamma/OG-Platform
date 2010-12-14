/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.context.Lifecycle;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;

/**
 * Exposes engine-level access to a view processor, including access to data structures which should not be available
 * externally.
 */
public interface ViewProcessorInternal extends ViewProcessor, Lifecycle {

  /**
   * Obtains a {@link ViewInternal} instance.
   * 
   * @param name  the name of the view to obtain, not null
   * @param credentials  the user attempting to access the view, not null
   * @return  the view
   */
  ViewInternal getView(String name, UserPrincipal credentials);
  
  /**
   * Gets the function compilation service
   * 
   * @return the function compilation service
   */
  CompiledFunctionService getFunctionCompilationService();
  
  /**
   * Gets the live data client
   * 
   * @return the live data client
   */
  LiveDataClient getLiveDataClient();
 
  /**
   * Gets the live data availability provider
   * 
   * @return the live data availability provider
   */
  LiveDataAvailabilityProvider getLiveDataAvailabilityProvider();
  
  /**
   * Gets the live data snapshot provider
   * 
   * @return the live data snapshot provider
   */
  LiveDataSnapshotProvider getLiveDataSnapshotProvider();
  
  /**
   * Gets the source of positions
   * 
   * @return the source of positions
   */
  PositionSource getPositionSource();
  
  /**
   * Gets the source of securities

   * @return the source of securities
   */
  SecuritySource getSecuritySource();
  
  /**
   * Gets the computation cache source
   * 
   * @return the computation cache source
   */
  ViewComputationCacheSource getComputationCacheSource();
  
  /**
   * Gets the computation job dispatcher
   * 
   * @return the computation job dispatcher
   */
  JobDispatcher getComputationJobDispatcher();
  
  /**
   * Gets the view processor query receiver
   * 
   * @return the view processor query receiver
   */
  ViewProcessorQueryReceiver getViewProcessorQueryReceiver();
  
  /**
   * Gets the dependency graph executor factory
   * 
   * @return the dependency graph executor factory
   */
  DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory();
  
  /**
   * Gets the view permission provider
   * 
   * @return the view permission provider
   */
  ViewPermissionProvider getViewPermissionProvider();
  
  /**
   * Requests the view processor temporarily stop processing of any active views at the end of the currently executing
   * cycle. This is to allow shared data such as configuration to be changed while retaining consistency for executing
   * views. The {@link Runnable} object returned by the {@link Future} will be called to indicate when operation can
   * resume.
   * 
   * @param executorService an executor for parallel shutdown of views
   * @return a future for the caller to use to detect when processing has stopped, and can use to notify the view
   *         processor later when processing can resume
   */
  Future<Runnable> suspend(ExecutorService executorService);
  
}
