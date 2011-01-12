/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
