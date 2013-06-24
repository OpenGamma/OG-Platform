/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.concurrent.Future;

import com.opengamma.util.async.Cancelable;

/**
 * Evaluates a dependency graph.
 */
public interface DependencyGraphExecutionFuture extends Cancelable, Future<String> {

  /**
   * Callback interface to receive notification of completion of this future.
   */
  interface Listener {

    void graphCompleted(String calculationConfiguration);

  }

  void setListener(Listener listener);

}
