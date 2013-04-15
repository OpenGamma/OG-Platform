/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Enumerate the state of default that an obligor is in
 */
public enum ObligorDefaultState {
  /**
   * Not defaulted
   */
  NOTDEFAULTED,
  /**
   * Defaulted
   */
  DEFAULTED;

  // TODO : Add finer detail to these states e.g. in process of restructuring etc
}
