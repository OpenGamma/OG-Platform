/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.trace;

import java.lang.reflect.Method;

import org.threeten.bp.Duration;

/**
 * Tracer implementation that does nothing.
 */
final class NoOpTracer extends Tracer {

  /**
   * Singleton implementation of the tracer.
   */
  static final Tracer INSTANCE = new NoOpTracer();

  /**
   * Restricted constructor.
   */
  private NoOpTracer() {
  }

  //-------------------------------------------------------------------------
  @Override
  void called(Method method, Object[] args) {
    // do nothing
  }

  @Override
  void returned(Object returnValue, Duration duration) {
    // do nothing
  }

  @Override
  void threw(Throwable ex, Duration duration) {
    // do nothing
  }

  @Override
  public CallGraphBuilder getRoot() {
    return null;
  }

}
