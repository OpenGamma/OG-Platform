/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

/**
 * Convention for yields.
 */
public interface YieldConvention {
  // TODO: supply whatever is needed to do a proper calculation

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  String getConventionName();

}
