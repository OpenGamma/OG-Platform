/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Represents a currency conversion matrix.
 */
@Config(description = "Currency matrix", group = ConfigGroups.CURRENCY)
public interface CurrencyMatrix extends UniqueIdentifiable {

  /**
   * Returns the set of "source" currencies defined in this matrix.
   * 
   * @return the set of source currencies, not null
   */
  Set<Currency> getSourceCurrencies();

  /**
   * Returns the set of "target" currencies defined in this matrix.
   * 
   * @return the set of target currencies, not null
   */
  Set<Currency> getTargetCurrencies();

  /**
   * Returns the conversion value for a source to a target currency. The conversion value is:
   * a) the number of units of the source currency for one unit of the target currency; or
   * b) a unique identifier for which another function can produce (a); or
   * c) an intermediate currency; or
   * d) null if no conversion is available
   * 
   * @param source the source currency, not null
   * @param target the target currency, not null
   * @return the matrix value
   */
  CurrencyMatrixValue getConversion(Currency source, Currency target);

}
