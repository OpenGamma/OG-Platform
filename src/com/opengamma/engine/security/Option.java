/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public interface Option {
  public OptionType getOptionType();
  public double getStrike();
  public Expiry getExpiry();
}
