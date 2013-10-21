/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;

/**
 * Abstraction of a "job" that will perform work as part of a {@link ViewProcess}. A job implementation may be a dedicated thread that executes the view, a proxy to a pool of threads that share the
 * view execution, or a proxy to a worker on a remote node.
 */
public interface ViewProcessWorker {

  /**
   * Triggers the next cycle in the execution sequence. The cycle will always be triggered regardless of the time since the previous one. To throttle cycle rates according to the view definition use
   * {@link #requestCycle}.
   * 
   * @return true if the trigger cycle request was respected, false if it was not
   */
  boolean triggerCycle();

  /**
   * Requests the next cycle in the execution sequence. The request will be ignored if the view definition includes throttles that limit the cycle execution rate. To ignore such throttles and execute
   * the cycle regardless use {@link #triggerCycle}.
   * 
   * @return true if the cycle request was respected, false if it was not
   */
  boolean requestCycle();

  /**
   * Notifies the job of an updated view definition.
   * <p>
   * View definitions are pushed into jobs rather than have the individual jobs subscribe to change events and pull new definitions. This allows the update to be coordinated among a number of
   * concurrent jobs at a suitable barrier.
   * <p>
   * This will be called from the change notification callback so must complete quickly. If the update operation required is slow and performed out of thread it is important that the order of update
   * messages is preserved, this may be by discarding superceded calls.
   * 
   * @param viewDefinition the new view definition, not null
   */
  void updateViewDefinition(ViewDefinition viewDefinition);

  /**
   * Terminate the executing job as soon as possible. The job must cease work on the view, cancel any jobs or requests that could cause work elsewhere, and allow any threads involved in the work to
   * terminate or be returned to their respective pools.
   * <p>
   * This is an asynchronous termination request. To wait for the job to actually terminate follow this with a call to {@link #join}.
   */
  void terminate();

  /**
   * Wait for completion of any threads working directly or indirectly on this job. After this call completes, the job should not make any more callbacks to its owning process.
   * 
   * @throws InterruptedException if the thread was interrupted before it could complete the operation
   */
  void join() throws InterruptedException;

  /**
   * Wait for completion of any threads working directly or indirectly on this job. After this call completes, returning true, the job should not make any more callbacks to its owning process.
   * 
   * @param timeout the maximum number of milliseconds to wait for the threads to join
   * @return true if the join completed, false if the timeout elapsed
   * @throws InterruptedException if the thread was interrupted before it could complete the operation
   */
  boolean join(long timeout) throws InterruptedException;

  /**
   * Tests if the job has terminated, either from reaching the end of the execution sequence or a direct call to {@link #terminate}.
   * 
   * @return true if the job has terminated, false otherwise
   */
  boolean isTerminated();

  /**
   * Forces the graph to be rebuilt during the next cycle. Do not use this method, it's a workaround for PLAT-3908
   * @deprecated Will be removed once PLAT-3908 has been fixed
   */
  @Deprecated
  void forceGraphRebuild();
}
