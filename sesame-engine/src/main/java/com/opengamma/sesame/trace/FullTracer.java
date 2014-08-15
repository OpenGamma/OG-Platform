/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.trace;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;

import org.threeten.bp.Duration;

/**
 * Tracer implementation that creates a full call graph.
 */
class FullTracer extends Tracer {

  /**
   * The stack of calls.
   */
  private final Deque<CallGraphBuilder> _stack = new LinkedList<>();
  /**
   * The root of the call graph.
   */
  private CallGraphBuilder _root;

  //-------------------------------------------------------------------------
  @Override
  void called(Method method, Object[] args) {
    CallGraphBuilder callGraphBuilder = new CallGraphBuilder(method, args);
    if (_stack.isEmpty()) {
      _root = callGraphBuilder;
    } else {
      _stack.peek().called(callGraphBuilder);
    }
    _stack.push(callGraphBuilder);
  }

  @Override
  void returned(Object returnValue, Duration duration) {
    _stack.pop().returned(returnValue, duration);
  }

  @Override
  void threw(Throwable ex, Duration duration) {
    _stack.pop().threw(ex, duration);
  }

  @Override
  public CallGraphBuilder getRoot() {
    return _root;
  }

}
