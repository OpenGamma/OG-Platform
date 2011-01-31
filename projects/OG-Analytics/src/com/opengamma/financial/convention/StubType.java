/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * The type of the stub.
 */
public enum StubType {

  /**
   * No stub.
   */
  NONE,
  /**
   * Short stub at the start of the schedule.
   */
  SHORT_START,
  /**
   * Long stub at the start of the schedule.
   */
  LONG_START,
  /**
   * Short stub at the end of the schedule.
   */
  SHORT_END,
  /**
   * Long stub at the end of the schedule.
   */
  LONG_END

}
