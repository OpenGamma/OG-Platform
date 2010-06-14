/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Factory to obtain instances of {@code Frequency}.
 */
public final class FrequencyFactory {

  /**
   * Singleton instance.
   */
  public static final FrequencyFactory INSTANCE = new FrequencyFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, Frequency> _conventionMap = new HashMap<String, Frequency>();

  /**
   * Creates the factory.
   */
  private FrequencyFactory() {
    store(SimpleFrequency.ANNUAL);
    store(SimpleFrequency.SEMI_ANNUAL);
    store(SimpleFrequency.QUARTERLY);
    store(SimpleFrequency.BIMONTHLY);
    store(SimpleFrequency.MONTHLY);
    store(SimpleFrequency.BIWEEKLY);
    store(SimpleFrequency.WEEKLY);
    store(SimpleFrequency.DAILY);
    store(SimpleFrequency.CONTINUOUS);
  }

  /**
   * Stores the convention.
   * @param convention  the convention to store, not null
   */
  private void store(final Frequency convention) {
    _conventionMap.put(convention.getConventionName().toLowerCase(Locale.ENGLISH), convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public Frequency getFrequency(final String name) {
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }

}
