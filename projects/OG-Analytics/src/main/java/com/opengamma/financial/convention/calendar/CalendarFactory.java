/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.convert.FromString;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 * Factory to obtain instances of {@code Calendar}.
 * <p>
 * The holidays and country details are read from a properties file.
 */
public final class CalendarFactory
    extends AbstractNamedInstanceFactory<Calendar> {

  // REVIEW: This is really quite a bad implementation. Bank Holiday dates need to be pulled
  // from a database or a more easily updated source. It should probably be possible
  // to update the data with the system running instead of at initialization.
  // emcleod 20-8-2013 This factory is only used in testing, so can probably be safely
  // deleted

  /**
   * Singleton instance.
   */
  public static final CalendarFactory INSTANCE = new CalendarFactory();

  /**
   * Map of calendar by country.
   */
  private final Map<String, Calendar> _countryMap = new HashMap<>();

  //-------------------------------------------------------------------------
  /**
   * Finds a convention by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static Calendar of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, loading the properties file.
   */
  private CalendarFactory() {
    super(Calendar.class);
    loadCalendarInstances();
    loadCountryDefinitions();
  }

  private void loadCalendarInstances() {
    final ResourceBundle calendars = ResourceBundle.getBundle(Calendar.class.getName());
    for (final String calendarName : calendars.keySet()) {
      try {
        String uri = null;
        String clazzName = calendars.getString(calendarName);
        if (clazzName.indexOf(':') > 0) {
          uri = clazzName.substring(clazzName.indexOf(':') + 1);
          final URL url = getClass().getClassLoader().getResource(uri);
          if (url != null) {
            uri = url.toString();
          }
          clazzName = clazzName.substring(0, clazzName.indexOf(':'));
        }
        final Class<? extends Calendar> clazz = Class.forName(clazzName).asSubclass(Calendar.class);
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        int noArgConstructor = -1;
        int nameConstructor = -1;
        int nameURIConstructor = -1;
        for (int i = 0; i < constructors.length; i++) {
          final Class<?>[] params = constructors[i].getParameterTypes();
          if (params.length == 0) {
            noArgConstructor = i;
          } else if (params.length == 1) {
            if (params[0].equals(String.class)) {
              nameConstructor = i;
            }
          } else if (params.length == 2) {
            if (params[0].equals(String.class) && params[1].equals(String.class)) {
              nameURIConstructor = i;
            }
          }
        }
        Calendar instance;
        if ((uri != null) && (nameURIConstructor >= 0)) {
          instance = (Calendar) constructors[nameURIConstructor].newInstance(calendarName, uri);
        } else if (nameConstructor >= 0) {
          instance = (Calendar) constructors[nameConstructor].newInstance(calendarName);
        } else if (noArgConstructor >= 0) {
          instance = (Calendar) constructors[noArgConstructor].newInstance();
        } else {
          throw new OpenGammaRuntimeException("No suitable constructor for '" + calendarName + "'");
        }
        addInstance(instance);
      } catch (final InstantiationException ex) {
        throw new OpenGammaRuntimeException("Error initialising Calendars", ex);
      } catch (final IllegalAccessException ex) {
        throw new OpenGammaRuntimeException("Error initialising Calendars", ex);
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Error initialising Calendars", ex);
      } catch (final IllegalArgumentException ex) {
        throw new OpenGammaRuntimeException("Error initialising Calendars", ex);
      } catch (final InvocationTargetException ex) {
        throw new OpenGammaRuntimeException("Error initialising Calendars", ex);
      }
    }
  }

  private void loadCountryDefinitions() {
    final ResourceBundle countries = ResourceBundle.getBundle("com.opengamma.financial.convention.calendar.Country");
    for (final String countryCode : countries.keySet()) {
      final String calendarName = countries.getString(countryCode);
      try {
        final Calendar calendar = instance(calendarName);
        _countryMap.put(countryCode, calendar);
      } catch (RuntimeException ex) {
        throw new OpenGammaRuntimeException("Cannot find calendar '" + calendarName + "' for country '" + countryCode + "'");
      }
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a working day calendar by name.
   * Matching is case insensitive.
   *
   * @param name  the name, not null
   * @return the convention, null if not found
   * @deprecated Use {@link #of(String)} or {@link #instance(String)}.
   */
  @Deprecated
  public Calendar getCalendar(final String name) {
    try {
      return instance(name);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Gets a working day calendar by 3-letter country code.
   *
   * @param country  the country code, not null
   * @return the convention, null if not found
   */
  public Calendar getCalendarByCountry(final String country) {
    return _countryMap.get(country);
  }

}
