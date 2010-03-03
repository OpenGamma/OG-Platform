/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory for getting/creating Calendar instances. Calendar names are read from
 * a Calendar resource. Mappings from country names to Calendars are read from a
 * Country resource.
 * 
 * This is really quite a bad implementation. Bank Holiday dates need to be pulled
 * from a database or a more easily updatable source. It should probably be possible
 * to update the data with the system running instead of at initialisation.
 * 
 * @author Andrew Griffin
 */
public class CalendarFactory {
  
  public static final CalendarFactory INSTANCE = new CalendarFactory ();
  
  private final Map<String,Calendar> _calendarMap = new HashMap<String,Calendar> ();
  private final Map<String,Calendar> _countryMap = new HashMap<String,Calendar> ();
  
  @SuppressWarnings("unchecked")
  private void loadCalendarInstances () {
    final ResourceBundle calendars = ResourceBundle.getBundle ("com.opengamma.financial.convention.calendar.Calendar");
    for (final String calendarName : calendars.keySet ()) {
      try {
        String uri = null;
        String clazzName = calendars.getString (calendarName);
        if (clazzName.indexOf (':') > 0) {
          uri = clazzName.substring (clazzName.indexOf (':') + 1);
          final URL url = getClass ().getClassLoader ().getResource (uri);
          if (url != null) uri = url.toString ();
          clazzName = clazzName.substring (0, clazzName.indexOf (':'));
        }
        final Class<Calendar> clazz = (Class<Calendar>)Class.forName (clazzName);
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors ();
        int noArgConstructor = -1;
        int nameConstructor = -1;
        int nameURIConstructor = -1;
        for (int i = 0; i < constructors.length; i++) {
          final Class<?>[] params = constructors[i].getParameterTypes ();
          if (params.length == 0) {
            noArgConstructor = i;
          } else if (params.length == 1) {
            if (params[0].equals (String.class)) {
              nameConstructor = i;
            }
          } else if (params.length == 2) {
            if (params[0].equals (String.class) && params[1].equals (String.class)) {
              nameURIConstructor = i;
            }
          }
        }
        Calendar instance;
        if ((uri != null) && (nameURIConstructor >= 0)) {
          instance = (Calendar)constructors[nameURIConstructor].newInstance (calendarName, uri);
        } else if (nameConstructor >= 0) {
          instance = (Calendar)constructors[nameConstructor].newInstance (calendarName);
        } else if (noArgConstructor >= 0) {
          instance = (Calendar)constructors[noArgConstructor].newInstance ();
        } else {
          throw new OpenGammaRuntimeException ("No suitable constructor for '" + calendarName + "'");
        }
        _calendarMap.put (calendarName.toLowerCase (), instance);
      } catch (InstantiationException e) {
        throw new OpenGammaRuntimeException ("Error initialising Calendars", e);
      } catch (IllegalAccessException e) {
        throw new OpenGammaRuntimeException ("Error initialising Calendars", e);
      } catch (ClassNotFoundException e) {
        throw new OpenGammaRuntimeException ("Error initialising Calendars", e);
      } catch (IllegalArgumentException e) {
        throw new OpenGammaRuntimeException ("Error initialising Calendars", e);
      } catch (InvocationTargetException e) {
        throw new OpenGammaRuntimeException ("Error initialising Calendars", e);
      }
    }
  }
  
  private void loadCountryDefinitions () {
    final ResourceBundle countries = ResourceBundle.getBundle ("com.opengamma.financial.convention.calendar.Country");
    for (final String countryCode : countries.keySet ()) {
      final String calendarName = countries.getString (countryCode);
      final Calendar calendar = getCalendar (calendarName);
      if (calendar == null) throw new OpenGammaRuntimeException ("Cannot find calendar '" + calendarName + "' for country '" + countryCode + "'");
      _countryMap.put (countryCode, calendar);
    }
  }
  
  private CalendarFactory () {
    loadCalendarInstances ();
    loadCountryDefinitions ();
  }
  
  /**
   * Returns a working day calendar based on the symbolic name. Note that the lookup is not case sensitive.
   */
  public Calendar getCalendar (final String name) {
    return _calendarMap.get (name.toLowerCase ());
  }
  
  /**
   * Returns a working day calendar associated with a country's ISO code.
   */
  public Calendar getCalendarByCountry (final String country) {
    return _countryMap.get (country);
  }
  
}