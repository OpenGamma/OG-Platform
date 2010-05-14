/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

public interface PositionGreekResult<T> {

  public boolean isMultiValued();

  public T getResult();
}
