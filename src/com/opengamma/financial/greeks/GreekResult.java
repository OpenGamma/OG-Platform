/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

public interface GreekResult<T> {

  public boolean isMultiValued();

  public T getResult();
}
