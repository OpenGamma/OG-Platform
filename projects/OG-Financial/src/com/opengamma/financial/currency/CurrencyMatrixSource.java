/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

/**
 * Represents a source of currency conversion matrices ({@link CurrencyMatrix}).
 */
public interface CurrencyMatrixSource {

  /**
   * Returns a currency conversion matrix.
   * 
   * @return the matrix
   */
  CurrencyMatrix getCurrencyMatrix();

  // TODO Do we need the concept of naming them, or referring to them by a unique identifier? When would it make sense to have two matrices for a given view processor?

}
