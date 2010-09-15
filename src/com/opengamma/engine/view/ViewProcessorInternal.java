/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.ExecutorService;

import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.livedata.client.LiveDataClient;

/**
 * Exposes engine-level access to a view processor, including access to data structures which should not be available
 * externally.
 */
public interface ViewProcessorInternal extends ViewProcessor {

  /**
   * Gets the function compilation service
   * 
   * @return the function compilation service
   */
  FunctionCompilationService getFunctionCompilationService();
  
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
   * Gets the executor service
   * 
   * @return the executor service
   */
  ExecutorService getExecutorService();
  
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
  
}
