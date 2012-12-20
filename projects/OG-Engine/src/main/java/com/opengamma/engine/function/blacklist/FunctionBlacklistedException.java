/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.engine.function.FunctionInvoker;

/**
 * Exception that can be thrown to indicate that a blacklist rule was matched and function use was suppressed. A {@link FunctionInvoker} that queries an internal blacklist may throw this if the
 * function was matched so that the suppression can be specifically reported rather than treated as an incorrect or failed execution.
 */
public class FunctionBlacklistedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FunctionBlacklistedException() {
  }

}
