/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.TimeUnit;

/**
 * 
 *
 * @author kirk
 */
public interface JobCompletionRetriever {
  
  CalculationJobResult getNextCompletedNoWait();
  
  CalculationJobResult getNextCompleted(long timeout, TimeUnit unit);

}
