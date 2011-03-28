package com.opengamma.core.marketdatasnapshot;

/**
 * The type of market data that a {@link MarketDataValueSpecification} refers to.
 */
public enum MarketDataValueType {

  /**
   * A security.
   */
  SECURITY,
  /**
   * A simple type, effectively "anything else".
   */
  PRIMITIVE
}
