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
 * Factory to obtain instances of {@code SimpleFrequency}.
 */
public final class SimpleFrequencyFactory {

  /**
   * Singleton instance.
   */
  public static final SimpleFrequencyFactory INSTANCE = new SimpleFrequencyFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, SimpleFrequency> _conventionMap = new HashMap<String, SimpleFrequency>();
  private final Map<Integer, SimpleFrequency> _periodsMap = new HashMap<Integer, SimpleFrequency>();

  /**
   * Creates the factory.
   */
  private SimpleFrequencyFactory() {
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
  private void store(final SimpleFrequency convention) {
    _conventionMap.put(convention.getConventionName().toLowerCase(Locale.ENGLISH), convention);
    _periodsMap.put((int) convention.getPeriodsPerYear(), convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public SimpleFrequency getFrequency(final String name) {
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }
  
  /**
   * Gets a convention by the number of periods per year (often returned from Bloomberg like this).
   * @param periods number of periods per year - 0 means once at end
   * @return the convention, null if not found
   */
  public SimpleFrequency getFrequency(final int periods) {
    return _periodsMap.get(periods);
  }

}
