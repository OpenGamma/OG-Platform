/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Used to create sub-graphs.
 */
public interface DependencyNodeFilter {
  
  boolean accept(DependencyNode node);

}
