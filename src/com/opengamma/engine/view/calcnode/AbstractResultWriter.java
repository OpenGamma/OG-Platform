/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;


/**
 * 
 */
public abstract class AbstractResultWriter implements ResultWriter {
  
  @Override
  public List<CalculationJobItem> getItemsToExecute(CalculationNode node, CalculationJob job) {
    return job.getJobItems();
  }

}
