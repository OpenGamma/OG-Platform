/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.VersionCorrection;

/**
 * Represents a source of currency conversion matrices ({@link CurrencyMatrix}).
 */
public interface CurrencyMatrixSource extends Source<CurrencyMatrix>, ChangeProvider {

  /**
   * Returns a currency conversion matrix.
   * 
   * @param name the name of the matrix, not null
   * @param versionCorrection the version/correction of the matrix to retrieve, not null
   * @return the matrix, null if not found
   */
  CurrencyMatrix getCurrencyMatrix(String name, VersionCorrection versionCorrection);

}
