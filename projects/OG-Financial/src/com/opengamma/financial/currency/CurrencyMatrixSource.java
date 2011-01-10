/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
   * @param name of the matrix
   * @return the matrix
   */
  CurrencyMatrix getCurrencyMatrix(String name);

}
