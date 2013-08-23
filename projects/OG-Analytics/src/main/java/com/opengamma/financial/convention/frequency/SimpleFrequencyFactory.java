/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.convert.FromString;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;

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
  private final Map<String, SimpleFrequency> _conventionMap = new HashMap<>();
  /**
   * Map of periods per year to convention, only contains frequencies with an integer number of periods per year
   * (plus {@link SimpleFrequency#TWENTY_EIGHT_DAYS} with a key of 13 periods).
   */
  private final Map<Integer, SimpleFrequency> _periodsMap = new HashMap<>();
  /**
   * All the frequencies.
   */
  private final List<SimpleFrequency> _frequencies = Lists.newArrayList();

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   *
   * @param name  the name, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if not found
   */
  @FromString
  public static SimpleFrequency of(final String name) {
    final SimpleFrequency result = SimpleFrequencyFactory.INSTANCE.getFrequency(name);
    if (result == null) {
      throw new IllegalArgumentException("Unknown SimpleFrequency: " + name);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the factory.
   */
  private SimpleFrequencyFactory() {
    store(SimpleFrequency.NEVER);
    store(SimpleFrequency.ANNUAL, "12m", "1y", "Yearly");
    store(SimpleFrequency.SEMI_ANNUAL, "6m", "Half Yearly");
    store(SimpleFrequency.QUARTERLY, "3m");
    store(SimpleFrequency.BIMONTHLY, "2m");
    store(SimpleFrequency.MONTHLY, "1m");
    store(SimpleFrequency.TWENTY_EIGHT_DAYS, "28d");
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
    storeByPeriodCount(SimpleFrequency.DAILY,
                       SimpleFrequency.WEEKLY,
                       SimpleFrequency.BIWEEKLY,
                       SimpleFrequency.TWENTY_EIGHT_DAYS,
                       SimpleFrequency.MONTHLY,
                       SimpleFrequency.BIMONTHLY,
                       SimpleFrequency.QUARTERLY,
                       SimpleFrequency.SEMI_ANNUAL,
                       SimpleFrequency.ANNUAL);
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
    _frequencies.add(convention);
    Collections.sort(_frequencies, new Comparator<SimpleFrequency>() {
      @Override
      public int compare(final SimpleFrequency o1, final SimpleFrequency o2) {
        return (int) Math.signum(o2.getPeriodsPerYear() - o1.getPeriodsPerYear());
      }
    });
  }

  /**
   * Stores the frequencies keyed by the number of periods per year. This only really makes sense for periods
   * with an integer number of periods per year. It will fail if it is called with multiple frequencies whose
   * period counts round to the same integer.
   * @param frequencies The frequencies to keyed on their (integer) period count.
   */
  private void storeByPeriodCount(final SimpleFrequency... frequencies) {
    for (final SimpleFrequency frequency : frequencies) {
      final int periodsPerYear = (int) frequency.getPeriodsPerYear();
      // this check is to prevent a repeat of a bug where frequencies were overwritten by another frequency whose
      // non-integer period count rounded to the same integer
      if (_periodsMap.containsKey(periodsPerYear)) {
        final SimpleFrequency existingFrequency = _periodsMap.get(periodsPerYear);
        throw new OpenGammaRuntimeException("Cannot overwrite " + existingFrequency.getConventionName() +
                                                " with " + frequency.getConventionName());
      }
      _periodsMap.put(periodsPerYear, frequency);
    }
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
  public Iterator<SimpleFrequency> enumerateAvailableFrequencies() {
    return Iterators.unmodifiableIterator(_frequencies.iterator());
  }

}
