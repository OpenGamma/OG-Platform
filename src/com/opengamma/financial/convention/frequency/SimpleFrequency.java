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
  // TODO: should be an enum?

  /**
   * Monthly frequency.
   */
  public static final Frequency MONTHLY = new SimpleFrequency("monthly");
  /**
   * Annual frequency.
   */
  public static final Frequency ANNUAL = new SimpleFrequency("annual");
  /**
   * Semi-annual frequency.
   */
  public static final Frequency SEMI_ANNUAL = new SimpleFrequency("semiannual");
  /**
   * Quarterly frequency.
   */
  public static final Frequency QUARTERLY = new SimpleFrequency("quarterly");

  /**
   * The convention name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  protected SimpleFrequency(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  @Override
  public String getConventionName() {
    return _name;
  }

}
