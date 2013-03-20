/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.id.VersionCorrection;

/**
 * Represents a source of currency conversion matrices ({@link CurrencyMatrix}).
 */
public interface CurrencyMatrixSource {

  /**
   * Returns a currency conversion matrix.
   * 
   * @param name the name of the matrix, not null
   * @param versionCorrection the version/correction of the matrix to retrieve, not null
   * @return the matrix, null if not found
   */
  CurrencyMatrix getCurrencyMatrix(String name, VersionCorrection versionCorrection);

}
