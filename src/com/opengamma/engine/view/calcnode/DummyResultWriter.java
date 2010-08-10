/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.List;


/**
 * 
 */
public class DummyResultWriter implements ResultWriter, Serializable {
  
  @Override
  public List<CalculationJobItem> getItemsToExecute(CalculationNode node, CalculationJob job) {
    return job.getJobItems();
  }

  @Override
  public void write(CalculationNode node, CalculationJobResult result) {
  }

}
