/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import com.opengamma.util.PublicSPI;

/**
 * The type of change that occurred.
 */
@PublicSPI
public enum ChangeType {

  /**
   * An entity was added.
   */
  ADDED,
  /**
   * An entity was changed.
   */
  CHANGED,
  /**
   * An entity was removed.
   */
  REMOVED,

}
