/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

/**
 * 
 */
public enum BatchStatus {
  
  /**
   * Batch still running
   */
  RUNNING,
  
  /**
   * Batch not running, all results (and failures) written to database
   */
  COMPLETE

}
