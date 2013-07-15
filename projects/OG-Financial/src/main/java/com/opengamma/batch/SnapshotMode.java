/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch;

/**
 * Enumerates the snapshot data modes
 */
public enum SnapshotMode {

  /**
   * The snapshot data has been prepared before the batch run, and should not be rewritten to the database.
   */
  PREPARED,
  /**
   * The snapshot data has not been prepared before the batch run. It should be persisted to the database during the run.
   */
  WRITE_THROUGH

}
