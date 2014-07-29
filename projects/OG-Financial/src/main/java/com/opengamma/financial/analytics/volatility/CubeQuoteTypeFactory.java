/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import java.util.Locale;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for cube quote type named instances.
 */
public final class CubeQuoteTypeFactory extends AbstractNamedInstanceFactory<CubeQuoteType> {

  /**
   * Singleton instance of {@code CubeQuoteTypeFactory}.
   */
  public static final CubeQuoteTypeFactory INSTANCE = new CubeQuoteTypeFactory();

  /**
   * Restricted constructor.
   */
  private CubeQuoteTypeFactory() {
    super(CubeQuoteType.class);
    addInstance(CubeQuoteType.EXPIRY_MATURITY_RELATIVE_STRIKE, "ExpiryMaturityRelativeStrike");
    addInstance(CubeQuoteType.EXPIRY_MATURITY_MONEYNESS, "ExpiryMaturityMoneyness");
  }

  /**
   * Finds a cube quote type by name, ignoring case.
   * <p>
   * This method dynamically creates the quote type if it is missing.
   * @param name The name of the instance to find, not null
   * @return The cube quote type, null if not found
   */
  @FromString
  public CubeQuoteType of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final CubeQuoteType quoteType = new CubeQuoteType(name.toUpperCase(Locale.ENGLISH));
      return addInstance(quoteType);
    }
  }
}
