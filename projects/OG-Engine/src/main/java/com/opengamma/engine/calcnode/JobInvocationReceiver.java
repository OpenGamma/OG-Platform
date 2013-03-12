/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

/**
 * Callback interface to receive the result state of a {@link JobInvoker#invoke} call.
 */
public interface JobInvocationReceiver {

  /**
   * The job was executed. The result returned will indicate whether the individual job items were successful or not. A job may complete with all items having failed individually at the calculation
   * node.
   * 
   * @param result the result of the job execution
   */
  void jobCompleted(CalculationJobResult result);

  /**
   * The job may not have been executed successfully. The job may not have been run at all, run but crashed the remote calculation node, or the calculation node started but did not finish the
   * execution. It is also possible the the execution was completed successfully but a failure in the communication channel results in this instead of the {@link #jobCompleted} notification.
   * 
   * @param jobInvoker the invoker that the job failed at
   * @param computeNodeId the identifier of the individual compute node
   * @param exception extended exception information, possibly null
   */
  void jobFailed(JobInvoker jobInvoker, String computeNodeId, Exception exception);

}
