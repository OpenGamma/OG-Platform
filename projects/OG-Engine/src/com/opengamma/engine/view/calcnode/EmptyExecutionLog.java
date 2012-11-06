/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

import com.opengamma.util.log.LogEvent;

/**
 * Represents an empty execution log, mainly for testing.
 */
public class EmptyExecutionLog implements ExecutionLog {

  @Override
  public boolean hasError() {
    return false;
  }

  @Override
  public boolean hasWarn() {
    return false;
  }

  @Override
  public boolean hasInfo() {
    return false;
  }

  @Override
  public List<LogEvent> getEvents() {
    return null;
  }

  @Override
  public boolean hasException() {
    return false;
  }

  @Override
  public String getExceptionClass() {
    return null;
  }

  @Override
  public String getExceptionMessage() {
    return null;
  }

  @Override
  public String getExceptionStackTrace() {
    return null;
  }

}
