/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;

/**
 * Implementation of {@link ViewProcessWorkerFactory} for creating {@link SingleThreadViewProcessWorker} instances. Using this will result in each view process that has an executing view owning a
 * local thread which will coordinate graph building, market data subscription, and execution. Between view cycles, while waiting for the next cycle trigger, the thread will be blocked.
 * <p>
 * Note that depending on other aspects of system configuration the view process may at times use many additional threads. This can include, but is not limited to, graph building (the graph building
 * service may have a pool of threads which view processes can share) and view compilation (there is a pool of threads shared by all view processes for portfolio traversal).
 */
public class SingleThreadViewProcessWorkerFactory implements ViewProcessWorkerFactory {

  @Override
  public ViewProcessWorker createWorker(ViewProcessWorkerContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition) {
    return new SingleThreadViewProcessWorker(context, executionOptions, viewDefinition);
  }
}
