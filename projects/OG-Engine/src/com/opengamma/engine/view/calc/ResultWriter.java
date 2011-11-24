/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Writes results.
 */
public interface ResultWriter {

  /**
   * Writes results.
   * 
   * @param resultModel the results to save
   * @param depGraph  the context information, useful for determining
   *  which results to write and which to skip
   */
  void write(ViewResultModel resultModel);

}
