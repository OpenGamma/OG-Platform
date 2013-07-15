/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.context.Lifecycle;

import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;

/**
 * Exposes engine-level access to a view processor, including access to data structures which should not be available
 * externally.
 */
public interface ViewProcessorInternal extends ViewProcessor, Lifecycle {

  /**
   * Gets an unmodifiable snapshot of the view processes currently being managed by this view processor. A view process
   * could be in any state, and might have finished producing new results or even have been terminated. 
   * 
   * @return a collection of the current view processes, not null
   */
  Collection<? extends ViewProcess> getViewProcesses();
  
  /**
   * Gets an unmodifiable collection of the view clients currently being managed by this view processor. A view client
   * could be in any state, and might have been terminated.
   * 
   * @return a collection of the current view clients, not null
   */
  Collection<ViewClient> getViewClients();
  
  /**
   * Gets the function compilation service
   * 
   * @return the function compilation service, not null
   */
  CompiledFunctionService getFunctionCompilationService();
  
  /**
   * Gets the view processor event listener registry.
   * 
   * @return the view processor event listener registry, not null
   */
  ViewProcessorEventListenerRegistry getViewProcessorEventListenerRegistry();
  
  /**
   * Requests the view processor temporarily stop any active view processes at the end of their respective currently-
   * executing cycles. This is to allow shared data such as configuration to be changed while retaining consistency for
   * executing view processes. The {@link Runnable} object returned by the {@link Future} will be called to indicate
   * when operations can resume.
   * 
   * @param executorService an executor service for scheduling the concurrent shutdown of view processes, not null
   * @return a future for the caller to use to detect when processing has stopped, and to notify the view processor
   *         when processing can resume, not null
   */
  Future<Runnable> suspend(ExecutorService executorService);
  
}
