/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Set;

import com.opengamma.financial.pnl.UnderlyingType;

/**
 * 
 */
public interface Underlying {
  //TODO rename - isn't really an order if there's an underlying

  public int getOrder();

  public Set<UnderlyingType> getUnderlyings();
}
