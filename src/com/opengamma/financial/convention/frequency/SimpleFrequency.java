/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import com.opengamma.util.ArgumentChecker;

/**
 * A simple frequency implementation.
 */
public class SimpleFrequency implements Frequency {
  /**
   * Annual frequency.
   */
  public static final Frequency ANNUAL = new SimpleFrequency("annual", 1);
  /**
   * Semi-annual frequency.
   */
  public static final Frequency SEMI_ANNUAL = new SimpleFrequency("semiannual", 2);
  /**
   * Quarterly frequency.
   */
  public static final Frequency QUARTERLY = new SimpleFrequency("quarterly", 4);
  /**
   * Bi-Monthly frequency.
   */
  public static final Frequency BIMONTHLY = new SimpleFrequency("bi-monthly", 6);
  /**
   * Monthly frequency.
   */
  public static final Frequency MONTHLY = new SimpleFrequency("monthly", 12);
  /**
   * Bi-weekly frequency.
   */
  public static final Frequency BIWEEKLY = new SimpleFrequency("bi-weekly", 26);
  /**
   * weekly frequency.
   */
  public static final Frequency WEEKLY = new SimpleFrequency("weekly", 52);
  /**
   * daily frequency.
   */
  public static final Frequency DAILY = new SimpleFrequency("daily", 365);

  /**
   * continuous frequency.
   */
  //TODO where converting to/from say continuously compounded interest rates, can't use Integer.MAX_VALUE, but need different formula 
  public static final Frequency CONTINUOUS = new SimpleFrequency("continuous", Integer.MAX_VALUE);

  /**
   * The convention name.
   */
  private final String _name;
  private final int _freq;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   * @param freq the frequency
   */
  protected SimpleFrequency(final String name, final int freq) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
    _freq = freq;
  }

  //-------------------------------------------------------------------------
  @Override
  public String getConventionName() {
    return _name;
  }

  @Override
  public int getPeriodsPerYear() {
    return _freq;
  }

}
