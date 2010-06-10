/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.id.UniqueIdentifier;

/**
 * Represents a floating interest rate leg of a swap.
 */
public class FloatingInterestRateLeg extends InterestRateLeg {
  // REVIEW: jim 28-May-2010 -- we're not sure if this should be a unique id or a string, or what.
  private UniqueIdentifier _floatingIdentifier; 

  /**
   * @param floatingIdentifier the unique id of the object used to provide the floating rate
   */
  public FloatingInterestRateLeg(UniqueIdentifier floatingIdentifier) {
    super();
    Validate.notNull(floatingIdentifier);
    _floatingIdentifier = floatingIdentifier;
  }

  /**
   * @return the unique id of the object used to provide the floating rate
   */
  public UniqueIdentifier getFloatingIdentifier() {
    return _floatingIdentifier;
  }
}
