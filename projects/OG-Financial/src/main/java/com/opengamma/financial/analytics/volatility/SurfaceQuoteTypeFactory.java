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
 * Factory for surface quote type named instances.
 */
public final class SurfaceQuoteTypeFactory extends AbstractNamedInstanceFactory<SurfaceQuoteType> {

  /**
   * Singleton instance of {@code CubeQuoteTypeFactory}.
   */
  public static final SurfaceQuoteTypeFactory INSTANCE = new SurfaceQuoteTypeFactory();

  /**
   * Restricted constructor.
   */
  private SurfaceQuoteTypeFactory() {
    super(SurfaceQuoteType.class);
    addInstance(SurfaceQuoteType.EXPIRY_MATURITY, "ExpiryMaturity");
  }

  /**
   * Finds a surface quote type by name, ignoring case.
   * <p>
   * This method dynamically creates the quote type if it is missing.
   * @param name The name of the instance to find, not null
   * @return The surface quote type, null if not found
   */
  @FromString
  public SurfaceQuoteType of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final SurfaceQuoteType quoteType = new SurfaceQuoteType(name.toUpperCase(Locale.ENGLISH));
      return addInstance(quoteType);
    }
  }
}
