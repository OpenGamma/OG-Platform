/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;


/**
 * Writes a set of results from the grid node.
 */
public interface ResultWriter {
  
  /**
   * On the grid calculation node, you may want to in some way
   * modify the list of job items that was sent down to the grid
   * node from the master node. For example, you may want not to 
   * execute certain items because they have already been executed, 
   * or you may want to execute additional items. This method
   * allows you to do just that.
   * <p>
   * If entries are filtered, this means that they will really
   * not be executed at all - the shared computation cache
   * will not be modified as far as the excluded entries are concerned.
   * 
   * @param node The grid node the method is being run on
   * @param job The job to be (potentially) modified
   * @return A list of items actually to be executed on the node.
   * The list does not need to include just items from 
   * {@link CalculationJob#getJobItems()} - you can also add
   * completely new items of your own.
   * @see {@link CalculationJobItem#setWriteResults(boolean)
   */
  List<CalculationJobItem> getItemsToExecute(CalculationNode node, CalculationJob job);
  
  /**
   * Writes a set of results from the grid node.  
   * 
   * @param node The grid node the method is being run on
   * @param result The result to write. See {@link #getItemsToExecute}. 
   */
  void write(CalculationNode node, CalculationJobResult result);

}
