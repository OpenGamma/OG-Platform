/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

/**
 * An call-back interface for results of jobs dispatched by {@link JobDispatcher}.
 *
 * @author kirk
 */
public interface JobResultReceiver {
  
  void resultReceived(CalculationJobResult result);
  
}
