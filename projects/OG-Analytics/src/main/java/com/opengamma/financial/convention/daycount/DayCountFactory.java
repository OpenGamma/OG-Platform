/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 * Factory to obtain instances of {@code DayCount}.
 * <p>
 * The conventions are read from a properties file.
 */
public final class DayCountFactory
    extends AbstractNamedInstanceFactory<DayCount> {

  /**
   * Singleton instance.
   */
  public static final DayCountFactory INSTANCE = new DayCountFactory();

  //-------------------------------------------------------------------------
  /**
   * Finds a convention by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static DayCount of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, loading the properties file.
   */
  private DayCountFactory() {
    super(DayCount.class);
    loadFromProperties();
  }

}
