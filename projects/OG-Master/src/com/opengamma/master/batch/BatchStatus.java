/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.batch;

/**
 * Enum of batch statuses.
 */
public enum BatchStatus {

  /**
   * The batch is still running.
   */
  RUNNING,
  /**
   * The batch is complete and no longer running, with all results and
   * failures written to database.
   */
  COMPLETE

}
