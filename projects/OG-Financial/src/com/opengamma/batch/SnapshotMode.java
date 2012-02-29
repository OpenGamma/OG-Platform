package com.opengamma.batch;

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Represents snapshot data mode
 */
public enum SnapshotMode {
  /**
   * The snapshot data is expected to be populated upfront batch run.
   */
  PREPARED,
  /**
   * The snapshot data is no prepared upfront the batch run, and it should be written to DB on the fly
   */
  WRITE_THROUGH

}
