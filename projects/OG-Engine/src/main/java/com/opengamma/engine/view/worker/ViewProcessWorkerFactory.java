/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.execution.ViewExecutionOptions;

/**
 * Factory pattern for constructing {@link ViewProcessWorker} instances on behalf of local {@link ViewProcess} implementations based on the execution options.
 */
public interface ViewProcessWorkerFactory {

  /**
   * Creates a worker to execute the view for the process. The nature of the worker will be chosen based on system resources/capabilities, the execution options, and any attributes of the view
   * definition that can influence how best to execute it. Possible options include a local thread, a pool of local threads, and/or a proxy to remotely executing code.
   * <p>
   * The context passed to the job encapsulates all necessary details about the owning process and provides a feedback mechanism for the results of execution to be posted to.
   * 
   * @param context the owning context, not null
   * @param executionOptions the execution options from the calling view process, not null
   * @param viewDefinition the view definition that will be executed, not null
   * @return the job, not null
   */
  ViewProcessWorker createWorker(ViewProcessWorkerContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition);

}
