/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;

/**
 * Implementation of {@link ViewComputationJobFactory} for creating {@link SingleThreadViewComputationJob} instances. Using this will result in each view process that has an executing view owning a
 * local thread which will coordinate graph building, market data subscription, and execution. Between view cycles, while waiting for the next cycle trigger, the thread will be blocked.
 */
public class SingleThreadViewComputationJobFactory implements ViewComputationJobFactory {

  @Override
  public ViewComputationJob createJob(ViewComputationJobContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition) {
    return new SingleThreadViewComputationJob(context, executionOptions, viewDefinition);
  }
}
