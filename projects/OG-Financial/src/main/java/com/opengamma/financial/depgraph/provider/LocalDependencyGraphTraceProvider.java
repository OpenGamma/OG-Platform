/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import com.google.common.annotations.VisibleForTesting;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilder;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;

/**
 * Local implementation. Simply delegates to trace builder class.
 */
public class LocalDependencyGraphTraceProvider implements DependencyGraphTraceProvider {

  private final DependencyGraphTraceBuilder _traceBuilder;

  public LocalDependencyGraphTraceProvider(DependencyGraphTraceBuilder traceBuilder) {
    _traceBuilder = traceBuilder;
  }

  @VisibleForTesting
  DependencyGraphTraceBuilder getTraceBuilder() {
    return _traceBuilder;
  }

  @Override
  public DependencyGraphBuildTrace getTrace(DependencyGraphTraceBuilderProperties properties) {
    return _traceBuilder.build(properties);
  }

}
