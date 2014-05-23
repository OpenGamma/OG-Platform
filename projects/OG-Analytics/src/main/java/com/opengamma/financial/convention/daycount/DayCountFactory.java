/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.util.Iterator;

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

  // -------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * 
   * @param name  the name, not null
   * @return the convention, null if not found
   * @deprecated Use {@link #of(String)} or {@link #instance(String)}.
   */
  @Deprecated
  public DayCount getDayCount(final String name) {
    try {
      return instance(name);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Iterates over the available conventions. No particular ordering is specified and conventions may
   * exist in the system not provided by this factory that aren't included as part of this enumeration.
   * 
   * @return the available conventions, not null
   * @deprecated use {@link #instanceMap()}
   */
  @Deprecated
  public Iterator<DayCount> enumerateAvailableDayCounts() {
    return instanceMap().values().iterator();
  }

}
