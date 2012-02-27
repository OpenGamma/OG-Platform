/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Iterators;

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
  /**
   * Map of periods per year to convention.
   */
  private final Map<Integer, SimpleFrequency> _periodsMap = new HashMap<Integer, SimpleFrequency>();

  /**
   * Creates the factory.
   */
  private SimpleFrequencyFactory() {
    store(SimpleFrequency.ANNUAL, "12m", "Yearly");
    store(SimpleFrequency.SEMI_ANNUAL, "6m", "Half Yearly");
    store(SimpleFrequency.QUARTERLY, "3m");
    store(SimpleFrequency.BIMONTHLY, "2m");
    store(SimpleFrequency.MONTHLY, "1m");
    store(SimpleFrequency.BIWEEKLY, "2w");
    store(SimpleFrequency.WEEKLY, "1w");
    store(SimpleFrequency.DAILY);
    store(SimpleFrequency.CONTINUOUS);
    store(SimpleFrequency.FOUR_MONTHS);
    store(SimpleFrequency.FIVE_MONTHS);
    store(SimpleFrequency.SEVEN_MONTHS);
    store(SimpleFrequency.EIGHT_MONTHS);
    store(SimpleFrequency.NINE_MONTHS);
    store(SimpleFrequency.TEN_MONTHS);
    store(SimpleFrequency.ELEVEN_MONTHS);
  }

  /**
   * Stores the convention.
   * @param convention  the convention to store, not null
   */
  private void store(final SimpleFrequency convention, final String... alternativeNames) {
    _conventionMap.put(convention.getConventionName().toLowerCase(Locale.ENGLISH), convention);
    for (final String alternativeName : alternativeNames) {
      _conventionMap.put(alternativeName.toLowerCase(Locale.ENGLISH), convention);
    }
    _periodsMap.put((int) convention.getPeriodsPerYear(), convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * 
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public SimpleFrequency getFrequency(final String name) {
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Gets a convention by the number of periods per year.
   * <p>
   * Some underlying data systems use this representation for frequency.
   * 
   * @param periods  the number of periods per year, zero means once at end
   * @return the convention, null if not found
   */
  public SimpleFrequency getFrequency(final int periods) {
    return _periodsMap.get(periods);
  }

  /**
   * Iterates over the available frequencies. No particular ordering is specified and conventions may
   * exist in the system not provided by this factory that aren't included as part of this enumeration.
   * 
   * @return the available conventions, not null
   */
  public Iterator<? extends Frequency> enumerateAvailableFrequencies() {
    return Iterators.unmodifiableIterator(_periodsMap.values().iterator());
  }

}
