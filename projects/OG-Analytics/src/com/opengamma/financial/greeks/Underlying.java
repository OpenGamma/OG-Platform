/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
