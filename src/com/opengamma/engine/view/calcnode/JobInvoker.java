/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

/**
 * Something that can invoke a job when required by the JobDispatcher. Capabilities are calculated
 * at an invoker level so an invoker should only dispatch to a homogenous set of calculation nodes.
 */
public interface JobInvoker {

  /**
   * Determines whether the calculation node(s) this invoker is responsible for have the
   * capability to successfully invoke the job.
   *
   * @param jobSpec the job spec
   * @param items the items
   * @return zero if the job cannot be executed, otherwise a positive score. When multiple
   * invokers are able to execute a job, the invoker with the highest score is chosen.
   */
  int canInvoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items);

  /**
   * Invokes a job on a calculation node.
   * 
   * @param jobSpec the job spec
   * @param items the items
   * @param receiver the result receiver; must be signalled with either success or failure
   * @return {@code true} if the invoker has caused the job to execute or {@code false} if
   * capacity problems mean it cannot be executed. After returning {@code false} to the
   * dispatcher the invoker will be unregistered.
   */
  boolean invoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items, JobInvocationReceiver receiver);
  
  /**
   * Called after invocation failure for the invoker to notify the dispatch object if/when
   * it becomes available again.
   * 
   * @param callback the object the invoker should register itself with when it is ready to
   * receive {@link #invoke} calls again. 
   */
  void notifyWhenAvailable(JobInvokerRegister callback);

}
