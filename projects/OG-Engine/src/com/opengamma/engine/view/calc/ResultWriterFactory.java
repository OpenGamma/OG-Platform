/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.depgraph.DependencyGraph;

/**
 * Creates result writers {@link ResultWriter}
 */
public interface ResultWriterFactory {
  
  ResultWriter createResultWriter(DependencyGraph graph);

}
