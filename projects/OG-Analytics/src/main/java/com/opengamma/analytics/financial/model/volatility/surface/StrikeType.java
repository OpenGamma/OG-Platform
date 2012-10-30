/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;


/**
 * 
 */
public interface StrikeType {

  double value();

  StrikeType with(final double value);

}
