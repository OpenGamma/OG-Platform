/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
