/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.convert.FromString;

import com.google.common.collect.Iterators;
import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory to obtain instances of {@code DayCount}.
 * <p>
 * The conventions are read from a properties file.
 */
public final class DayCountFactory {

  /**
   * Singleton instance.
   */
  public static final DayCountFactory INSTANCE = new DayCountFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, DayCount> _conventionMap = new HashMap<>();

  /**
   * All convention instances.
   */
  private final Collection<DayCount> _conventions;

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
  public static DayCount of(final String name) {
    DayCount result = DayCountFactory.INSTANCE.getDayCount(name);
    if (result == null) {
      throw new IllegalArgumentException("Unknown DayCount: " + name);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the factory
   */
  private DayCountFactory() {
    final ResourceBundle conventions = ResourceBundle.getBundle(DayCount.class.getName());
    final Map<String, DayCount> instances = new HashMap<>();
    for (final String convention : conventions.keySet()) {
      final String clazz = conventions.getString(convention);
      DayCount instance = instances.get(clazz);
      if (instance == null) {
        try {
          instance = (DayCount) Class.forName(clazz).newInstance();
          instances.put(clazz, instance);
        } catch (InstantiationException ex) {
          throw new OpenGammaRuntimeException("Error initialising DayCount conventions", ex);
        } catch (IllegalAccessException ex) {
          throw new OpenGammaRuntimeException("Error initialising DayCount conventions", ex);
        } catch (ClassNotFoundException ex) {
          throw new OpenGammaRuntimeException("Error initialising DayCount conventions", ex);
        }
      }
      _conventionMap.put(convention.toLowerCase(Locale.ENGLISH), instance);
    }
    _conventions = new ArrayList<>(instances.values());
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * 
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public DayCount getDayCount(final String name) {
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Iterates over the available conventions. No particular ordering is specified and conventions may
   * exist in the system not provided by this factory that aren't included as part of this enumeration.
   * 
   * @return the available conventions, not null
   */
  public Iterator<DayCount> enumerateAvailableDayCounts() {
    return Iterators.unmodifiableIterator(_conventions.iterator());
  }

}
