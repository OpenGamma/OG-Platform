/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;


/**
 * Writes a set of results from the grid node.
 */
public interface ResultWriter {
  
  void write(AbstractCalculationNode node, CalculationJobResult result);

}
