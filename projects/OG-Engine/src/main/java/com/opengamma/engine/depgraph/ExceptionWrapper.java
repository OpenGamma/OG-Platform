/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

/**
 * Wraps an exception thrown by the graph building (e.g. from a function) in a way that allows them to be collated.
 * <p>
 * Exception equality is defined based on the top frame of the stack trace, message and the wrapped cause of the exception.
 */
/* package */final class ExceptionWrapper {

  private final Throwable _exception;
  private final String _message;
  private final StackTraceElement _topStackFrame;
  private final ExceptionWrapper _cause;
  private int _count;

  private ExceptionWrapper(final Throwable exception, final ExceptionWrapper cause) {
    _exception = exception;
    _message = exception.getMessage();
    final StackTraceElement[] trace = exception.getStackTrace();
    if (trace.length > 0) {
      _topStackFrame = trace[0];
    } else {
      _topStackFrame = null;
    }
    _cause = cause;
    _count = 1;
  }

  /**
   * Create a new wrapper instance and add it to the canonical map.
   * 
   * @param exception exception to wrap
   * @param canon the canonical map to add to
   */
  public static ExceptionWrapper createAndPut(final Throwable exception, final Map<ExceptionWrapper, ExceptionWrapper> canon) {
    final ExceptionWrapper instance;
    final Throwable cause = exception.getCause();
    if (cause != null) {
      instance = new ExceptionWrapper(exception, createAndPut(cause, canon));
    } else {
      instance = new ExceptionWrapper(exception, null);
    }
    final ExceptionWrapper existing = canon.get(instance);
    if (existing != null) {
      existing.incrementCount();
      return existing;
    } else {
      canon.put(instance, instance);
      return instance;
    }
  }

  public Throwable getException() {
    return _exception;
  }

  public int getCount() {
    return _count;
  }

  public void incrementCount() {
    _count++;
  }

  public void incrementCount(final int amount) {
    _count += amount;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ExceptionWrapper)) {
      return false;
    }
    final ExceptionWrapper other = (ExceptionWrapper) o;
    return ObjectUtils.equals(other._message, _message) && ObjectUtils.equals(other._topStackFrame, _topStackFrame) && ObjectUtils.equals(other._cause, _cause);
  }

  @Override
  public int hashCode() {
    return (ObjectUtils.hashCode(_message) * 17 + ObjectUtils.hashCode(_topStackFrame)) * 17 + ObjectUtils.hashCode(_cause);
  }

}
