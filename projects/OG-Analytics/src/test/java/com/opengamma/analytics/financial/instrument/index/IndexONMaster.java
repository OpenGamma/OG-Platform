/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Description of ON indexes available for tests. 
 */
public final class IndexONMaster {

  /**
   * The method unique instance.
   */
  private static final IndexONMaster INSTANCE = new IndexONMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static IndexONMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of Ibor Indexes and their conventions.
   */
  private final Map<String, IndexON> _on;

  /**
   * Private constructor.
   */
  private IndexONMaster() {
    Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _on = new HashMap<String, IndexON>();
    _on.put("EONIA", new IndexON("EONIA", Currency.EUR, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 0, baseCalendar));
    _on.put("FED FUND", new IndexON("FED FUND", Currency.USD, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 1, baseCalendar));
    _on.put("SONIA", new IndexON("SONIA", Currency.GBP, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, baseCalendar));
    _on.put("RBA ON", new IndexON("RBA ON", Currency.AUD, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, baseCalendar));
    _on.put("DKK TN", new IndexON("DKK TN", Currency.DKK, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 1, baseCalendar));
  }

  public IndexON getIndex(final String name, final Calendar cal) {
    IndexON indexNoCalendar = _on.get(name);
    if (indexNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get ON index for " + name);
    }
    return new IndexON(indexNoCalendar.getName(), indexNoCalendar.getCurrency(), indexNoCalendar.getDayCount(), indexNoCalendar.getPublicationLag(), cal);
  }

}
