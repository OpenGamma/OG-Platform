/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.convert.FromString;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 * Factory to obtain instances of {@code SimpleFrequency}.
 */
public final class SimpleFrequencyFactory
    extends AbstractNamedInstanceFactory<SimpleFrequency> {

  /**
   * Singleton instance.
   */
  public static final SimpleFrequencyFactory INSTANCE = new SimpleFrequencyFactory();
  /**
   * Map of periods per year to convention, only contains frequencies with an integer number of periods per year
   * (plus {@link SimpleFrequency#TWENTY_EIGHT_DAYS} with a key of 13 periods).
   */
  private final Map<Integer, SimpleFrequency> _periodsMap = new HashMap<>();

  //-------------------------------------------------------------------------
  /**
   * Finds a convention by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static SimpleFrequency of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, hard coding the conventions.
   */
  private SimpleFrequencyFactory() {
    super(SimpleFrequency.class);
    addInstance(SimpleFrequency.NEVER, "1t");
    addInstance(SimpleFrequency.ANNUAL, "12m", "1y", "Yearly");
    addInstance(SimpleFrequency.SEMI_ANNUAL, "6m", "Half Yearly");
    addInstance(SimpleFrequency.QUARTERLY, "3m");
    addInstance(SimpleFrequency.BIMONTHLY, "2m");
    addInstance(SimpleFrequency.MONTHLY, "1m");
    addInstance(SimpleFrequency.TWENTY_EIGHT_DAYS, "28d");
    addInstance(SimpleFrequency.BIWEEKLY, "2w");
    addInstance(SimpleFrequency.WEEKLY, "1w");
    addInstance(SimpleFrequency.DAILY, "1d");
    addInstance(SimpleFrequency.CONTINUOUS);
    addInstance(SimpleFrequency.FOUR_MONTHS, "4m");
    addInstance(SimpleFrequency.FIVE_MONTHS, "5m");
    addInstance(SimpleFrequency.SEVEN_MONTHS, "7m");
    addInstance(SimpleFrequency.EIGHT_MONTHS, "8m");
    addInstance(SimpleFrequency.NINE_MONTHS, "9m");
    addInstance(SimpleFrequency.TEN_MONTHS, "10m");
    addInstance(SimpleFrequency.ELEVEN_MONTHS, "11m");
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
        throw new OpenGammaRuntimeException("Cannot overwrite " + existingFrequency.getName() +
                                                " with " + frequency.getName());
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
   * @deprecated Use {@link #of(String)} or {@link #instance(String)}.
   */
  @Deprecated
  public SimpleFrequency getFrequency(final String name) {
    try {
      return instance(name);
    } catch (IllegalArgumentException ex) {
      return null;
    }
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
   * @deprecated Use {@link #instanceMap()}
   */
  @Deprecated
  public Iterator<SimpleFrequency> enumerateAvailableFrequencies() {
    List<SimpleFrequency> frequencies = new ArrayList<>(instanceMap().values());
    Collections.sort(frequencies, new Comparator<SimpleFrequency>() {
      @Override
      public int compare(final SimpleFrequency o1, final SimpleFrequency o2) {
        return (int) Math.signum(o2.getPeriodsPerYear() - o1.getPeriodsPerYear());
      }
    });
    return frequencies.iterator();
  }

}
