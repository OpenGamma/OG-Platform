/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * An option.
 */
public interface Option {

  /**
   * Gets the option type.
   * @return the option type
   */
  OptionType getOptionType();

  /**
   * Gets the strike.
   * @return the strike
   */
  double getStrike();

  /**
   * Gets the expiry.
   * @return the expiry
   */
  Expiry getExpiry();

  /**
   * Gets the underlying security.
   * @return the underlying security
   */
  Identifier getUnderlyingIdentifier();

}
