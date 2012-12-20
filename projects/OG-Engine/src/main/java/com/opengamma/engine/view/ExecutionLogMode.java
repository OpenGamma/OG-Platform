/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.util.PublicAPI;

/**
 * Enumerates the execution log modes.
 */
@PublicAPI
public enum ExecutionLogMode {

  /**
   * Include only log indicators to show which levels of log event occurred.
   */
  INDICATORS,
  /**
   * Include full log event details including the messages.
   */
  FULL
  
}
