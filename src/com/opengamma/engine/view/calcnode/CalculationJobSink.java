/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

/**
 * 
 *
 * @author kirk
 */
public interface CalculationJobSink {
  
  void invoke(CalculationJob job);

}
