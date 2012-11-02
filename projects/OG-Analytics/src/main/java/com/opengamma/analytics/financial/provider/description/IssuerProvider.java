/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import java.util.Set;

import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for issuer specific 
 */
public interface IssuerProvider {
  // TODO: Can probably be merged with MulticurveProvider if the Currency is replaced by a UniqueIdentifiable.

  /**
   * Gets the discount factor for one issuer in one currency.
   * @param issuerCcy The issuer name/currency pair.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(Pair<String, Currency> issuerCcy, Double time);

  /**
   * Gets the set of issuer names by currency defined in the market.
   * @return The set of issuers names/currencies.
   */
  Set<Pair<String, Currency>> getIssuersCcy();

}
