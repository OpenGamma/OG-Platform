/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * An option.
 */
public interface Option {

  /**
   * Gets the option type.
   * @return the option type
   */
  public OptionType getOptionType();

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike();

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public Expiry getExpiry();

  /**
   * Gets the underlying security.
   * @return the underlying security
   */
  public UniqueIdentifier getUnderlyingSecurity();

}
