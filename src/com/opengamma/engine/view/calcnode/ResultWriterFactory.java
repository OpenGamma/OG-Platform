/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

/**
 * Create a result writer which is sent down to a grid node.
 */
public interface ResultWriterFactory {
  
  /**
   * Creates a result writer. 
   * 
   * @param jobSpec What view, calc conf, etc., is being evaluated. Not null
   * @param items What graph nodes are being evaluated and in what order. Not null
   * @param nodeId Id of the node the job will be evaluated on. THIS MAY BE NULL if
   * the destination node is not known when the result writer is being created.
   * This will be the case with many grid systems as the grid will handle 
   * machine assignment. In this case, when the result writer is re-instantiated
   * on the grid, it needs to load the host information it needs at that point.
   * 
   * @return The result writer. The result writer needs to be Fudge-serializable,
   * i.e., have toFudgeMsg and static fromFudgeMsg methods.
   */
  ResultWriter create(CalculationJobSpecification jobSpec, 
      List<CalculationJobItem> items,
      String nodeId);

}
