/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

import com.opengamma.util.money.Currency;

/**
 * A source of volatility surface specifications.
 * <p>
 * This interface provides a simple view of volatility surface specifications.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface VolatilitySurfaceSpecificationSource {

  /**
   * Gets a volatility surface specification for a currency and name.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceSpecification getSpecification(Currency currency, String name);

  /**
   * Gets a volatility surface specification for a currency, name and version.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param version  the version instant, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceSpecification getSpecification(Currency currency, String name, Instant version);

}
