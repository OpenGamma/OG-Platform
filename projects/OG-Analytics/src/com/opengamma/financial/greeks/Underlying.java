/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.List;

import com.opengamma.financial.pnl.UnderlyingType;

/**
 * 
 */
public interface Underlying {

  int getOrder();

  List<UnderlyingType> getUnderlyings();
}
