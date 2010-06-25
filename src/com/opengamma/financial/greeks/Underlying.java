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

  int getOrder();

  Set<UnderlyingType> getUnderlyings();
}
