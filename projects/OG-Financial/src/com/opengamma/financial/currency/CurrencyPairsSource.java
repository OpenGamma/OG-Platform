package com.opengamma.financial.currency;

/**
 * A source of market convention currency pairs ({@link CurrencyPairs} instances).
 */
public interface CurrencyPairsSource {

  /**
   * @param name The name of the set of currency pairs
   * @return The currency pairs
   */
  CurrencyPairs getCurrencyPairs(String name);
}
