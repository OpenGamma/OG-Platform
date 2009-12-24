/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.AnalyticValueDefinition;

/**
 * Allows code to query a set of {@link DependencyNode}s to identify
 * one that already matches requirements.
 *
 * @author kirk
 */
public interface DependencyNodeResolver {
  
  DependencyNode resolve(AnalyticValueDefinition<?> outputValue);

}
