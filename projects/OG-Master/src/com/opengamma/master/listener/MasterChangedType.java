/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import com.opengamma.util.PublicSPI;

/**
 * The type of change that occurred to a master.
 */
@PublicSPI
public enum MasterChangedType {

  /**
   * An object was added.
   */
  ADDED,
  /**
   * An object was updated.
   */
  UPDATED,
  /**
   * An object was removed.
   */
  REMOVED,
  /**
   * An object was corrected.
   */
  CORRECTED,

}
