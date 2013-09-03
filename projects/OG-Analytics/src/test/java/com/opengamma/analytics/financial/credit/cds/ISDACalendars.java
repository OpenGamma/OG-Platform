/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Loads the calendars for the ISDA CDS tests
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDACalendars {
  private static final String RESOURCE_DIR = "resources";
  private static final String CALENDER_DIR = "isda_holiday_calendars";

  private static final String[] CALENDAR_FILES = {"TYO" };
  private static final Currency[] CURRENCY = {Currency.JPY };
  private static Map<Currency, ExceptionCalendar> s_calendars = new TreeMap<>();

  private static void loadData() throws IOException, URISyntaxException {
    for (int i = 0; i < CALENDAR_FILES.length; i++) {
      final String path = RESOURCE_DIR + "/" + CALENDER_DIR + "/" + CALENDAR_FILES[i] + ".xml";
      final URI uri = ISDACalendars.class.getClassLoader().getResource(path).toURI();
      final ExceptionCalendar cal = new MondayToFridayCalendar(CALENDAR_FILES[i], uri.toURL().toString());
      s_calendars.put(CURRENCY[i], cal);
    }
  }

  /**
   * Gets the calendars.
   * @return the calendars
   * @throws URISyntaxException
   * @throws IOException
   */
  public static Map<Currency, ExceptionCalendar> getCalendars() throws IOException, URISyntaxException {
    if (s_calendars == null || s_calendars.values().isEmpty()) {
      loadData();
    }
    return s_calendars;
  }
}
