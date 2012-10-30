/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

/**
 * Bloomberg ticks collection storage mode
 */
public enum StorageMode {
  /**
   * Store all ticks in a single file
   */
  SINGLE,
  /**
   * Store ticks per security
   */
  MULTI
}
