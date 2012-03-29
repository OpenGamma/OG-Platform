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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory to obtain instances of {@code Calendar}.
 * <p>
 * The holidays and country details are read from a properties file.
 */
public final class CalendarFactory {
  // TODO: This is really quite a bad implementation. Bank Holiday dates need to be pulled
  // from a database or a more easily updated source. It should probably be possible
  // to update the data with the system running instead of at initialization.

  /**
   * Singleton instance.
   */
  public static final CalendarFactory INSTANCE = new CalendarFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, Calendar> _calendarMap = new HashMap<String, Calendar>();
  private final Map<String, Calendar> _countryMap = new HashMap<String, Calendar>();

  /**
   * Creates the factory.
   */
  private CalendarFactory() {
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
        final Class<Calendar> clazz = (Class<Calendar>) Class.forName(clazzName);
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
        _calendarMap.put(calendarName.toLowerCase(), instance);
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
      final Calendar calendar = getCalendar(calendarName);
      if (calendar == null) {
        throw new OpenGammaRuntimeException("Cannot find calendar '" + calendarName + "' for country '" + countryCode + "'");
      }
      _countryMap.put(countryCode, calendar);
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a working day calendar by name.
   * Matching is case insensitive.
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public Calendar getCalendar(final String name) {
    return _calendarMap.get(name.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Gets a working day calendar by 3-letter country code.
   * @param country  the country code, not null
   * @return the convention, null if not found
   */
  public Calendar getCalendarByCountry(final String country) {
    return _countryMap.get(country);
  }

}
