/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;


/**
 * 
 */
public interface RolldownFunction<T> {

  T rollDownCurve(T curve, double time);
}
