/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

/**
 * An call-back interface for results of jobs dispatched by {@link JobDispatcher}.
 *
 * @author kirk
 */
public interface JobResultReceiver {
  
  void resultReceived(CalculationJobResult result);
  
}
