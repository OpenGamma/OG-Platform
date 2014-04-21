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
 * Factory for volatility quote unit named instances.
 */
public final class VolatilityQuoteUnitsFactory extends AbstractNamedInstanceFactory<VolatilityQuoteUnits> {

  /**
   * Singleton instance of {@code VolatilityQuoteUnitsFactory}.
   */
  public static final VolatilityQuoteUnitsFactory INSTANCE = new VolatilityQuoteUnitsFactory();

  /**
   * Restricted constructor.
   */
  private VolatilityQuoteUnitsFactory() {
    super(VolatilityQuoteUnits.class);
    addInstance(VolatilityQuoteUnits.LOGNORMAL, "Lognormal");
    addInstance(VolatilityQuoteUnits.NORMAL, "Normal");
    addInstance(VolatilityQuoteUnits.RATES, "Rates");
    addInstance(VolatilityQuoteUnits.DECIMALS, "Decimals");
    addInstance(VolatilityQuoteUnits.BASIS_POINTS, "Basis Points");
  }

  /**
   * Finds a volatility quote unit by name, ignoring case.
   * <p>
   * This method dynamically creates the quote units if it is missing.
   * @param name The name of the instance to find, not null
   * @return The volatility quote units, null if not found
   */
  @FromString
  public VolatilityQuoteUnits of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final VolatilityQuoteUnits quoteUnits = new VolatilityQuoteUnits(name.toUpperCase(Locale.ENGLISH));
      return addInstance(quoteUnits);
    }
  }
}
