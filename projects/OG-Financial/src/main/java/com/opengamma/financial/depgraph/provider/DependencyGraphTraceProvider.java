/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;

/**
 * Interface for retrieving instances of {@link DependencyGraphBuildTrace}.
 */
public interface DependencyGraphTraceProvider {

  /**
   * Gets a trace instance with the specified properties.
   * @param properties the properties to use
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTrace(DependencyGraphTraceBuilderProperties properties);

}
