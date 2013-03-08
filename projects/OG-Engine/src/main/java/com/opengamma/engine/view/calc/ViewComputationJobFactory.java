/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.execution.ViewExecutionOptions;

/**
 * Factory pattern for constructing {@link ViewComputationJob} instances on behalf of local {@link ViewProcess} implementations based on the execution options.
 */
public interface ViewComputationJobFactory {

  /**
   * Creates a job to execute the view for the process. The nature of the job will be chosen based on system resources/capabilities, the execution options, and any attributes of the view definition
   * that can influence how best to execute it.
   * <p>
   * The context passed to the job encapsulates all necessary details about the owning process and provides a feedback mechanism for the results of execution to be posted to.
   * 
   * @param context the owning context, not null
   * @param executionOptions the execution options from the calling view process, not null
   * @param viewDefinition the view definition that will be executed, not null
   * @return the job, not null
   */
  ViewComputationJob createJob(ViewComputationJobContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition);

}
