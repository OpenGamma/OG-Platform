/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Specifies the current state of a {@link View}.
 *
 * @author kirk
 */
public enum ViewCalculationState {
  NOT_INITIALIZED,
  INITIALIZING,
  NOT_STARTED,
  STARTING,
  RUNNING,
  TERMINATING,
  TERMINATED;
}
