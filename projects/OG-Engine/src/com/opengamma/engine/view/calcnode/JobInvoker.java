/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

/**
 * Something that can invoke a job when required by the JobDispatcher. Capabilities are calculated
 * at an invoker level so an invoker should only dispatch to a homogeneous set of calculation nodes.
 * The calculation nodes a given job invoker dispatches to are assumed to share the same local
 * cache.
 */
public interface JobInvoker {

  /**
   * Returns the exported capabilities of the node(s) this invoker is responsible for. These will
   * be used to determine which jobs will get farmed to this invoker. It will not be modified by
   * the job dispatcher. An iterator on the collection must return the capabilities in their natural
   * order. It should have very efficient hashCode and equals operations.
   * 
   * @return the capabilities, not {@code null}
   */
  Collection<Capability> getCapabilities();

  /**
   * Invokes a job on a calculation node.
   * 
   * @param job the job to run
   * @param receiver the result receiver; must be signaled with either success or failure unless
   * {@code false} is returned to indicate the invoker did not accept the job.
   * @return {@code true} if the invoker has caused the job to execute or {@code false} if
   * capacity problems mean it cannot be executed. After returning {@code false} to the
   * dispatcher the invoker will be unregistered.
   */
  boolean invoke(CalculationJob job, JobInvocationReceiver receiver);

  /**
   * Called after invocation failure for the invoker to notify the dispatch object if/when
   * it becomes available again.
   * 
   * @param callback the object the invoker should register itself with when it is ready to
   * receive {@link #invoke} calls again. This must not be called inline from this method,
   * if the invoker is ready it must return {@code true}.
   * @return return {@code false} if the callback will be invoked in the future. If the
   * invoker is ready now, {@code true}.  
   */
  boolean notifyWhenAvailable(JobInvokerRegister callback);

  String getInvokerId();

  /**
   * Attempts to cancel the set of jobs previously started by a call to {@link invoke}. After
   * cancellation the job should not generate a callback to the invocation receiver, but may
   * do so if cancellation is not possible.
   * 
   * @param jobs jobs to cancel
   */
  void cancel(Collection<CalculationJobSpecification> jobs);

  /**
   * Queries the status of jobs on the invoker. This can be used as a "hint" or nudge to help
   * failed nodes abort sooner and allow calculation to resume elsewhere.
   * 
   * @param jobs outstanding jobs thought to be still running with this invoker
   * @return {@code true} if the invoker is confident the jobs will complete, {@code false} if
   * the jobs must be considered "timed-out" and re-dispatched. This method must not block or
   * take long to complete. If determining status is costly, the node should return {@code true}
   * and use {@link JobInvocationReceiver#jobFailed(JobInvoker, String, Exception)}
   * asynchronously.
   */
  boolean isAlive(Collection<CalculationJobSpecification> jobs);

}
