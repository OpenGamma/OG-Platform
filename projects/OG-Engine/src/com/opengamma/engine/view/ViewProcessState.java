/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.util.PublicAPI;

/**
 * Enumerates the computation states of a {@link ViewProcess}.
 */
@PublicAPI
public enum ViewProcessState {
  /**
   * The computation job is not running. There may be more computation cycles still to run.
   */
  STOPPED,
  /**
   * The computation job is running.
   */
  RUNNING,
  /**
   * The computation job has completed. Jobs which provide an infinite sequence of evaluation times will never
   * complete.
   */
  FINISHED,
  /**
   * The view has been terminated.
   */
  TERMINATED
}
