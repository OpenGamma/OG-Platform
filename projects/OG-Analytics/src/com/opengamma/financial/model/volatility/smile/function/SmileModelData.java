/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

/**
 * 
 */
public interface SmileModelData {

  int getNumberOfparameters();

  double getParameter(final int index);

  SmileModelData with(final int index, final double value);
  
  
  
}
