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
public interface JobCompletionNotifier {
  
  // TODO kirk 2009-09-25 -- Needs a way to send errors or diagnostics back.
  
  void jobCompleted(CalculationNodeJob job);

}
