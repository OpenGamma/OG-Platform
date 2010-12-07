/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.DayCount;

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
  private final Map<String, DayCount> _conventionMap = new HashMap<String, DayCount>();

  /**
   * Creates the factory
   */
  private DayCountFactory() {
    final ResourceBundle conventions = ResourceBundle.getBundle(DayCountFactory.class.getName());
    final Map<String, DayCount> instances = new HashMap<String, DayCount>();
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
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public DayCount getDayCount(final String name) {
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }

}
