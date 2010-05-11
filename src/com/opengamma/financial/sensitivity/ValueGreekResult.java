/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

public interface ValueGreekResult<T> {

  public boolean isMultiValued();

  public T getResult();

}
