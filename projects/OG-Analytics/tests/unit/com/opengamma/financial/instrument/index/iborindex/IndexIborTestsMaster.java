/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.iborindex;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class IndexIborTestsMaster {

  /**
   * The method unique instance.
   */
  private static final IndexIborTestsMaster INSTANCE = new IndexIborTestsMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static IndexIborTestsMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of Ibor Indexes and their conventions.
   */
  private final Map<String, IborIndex> _ibor;

  /**
   * Private constructor.
   */
  private IndexIborTestsMaster() {
    Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _ibor = new HashMap<String, IborIndex>();
    _ibor.put(
        "AUDBB3M",
        new IborIndex(Currency.AUD, Period.ofMonths(3), 1, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "AUDBB3M"));
    _ibor.put(
        "AUDBB6M",
        new IborIndex(Currency.AUD, Period.ofMonths(6), 1, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "AUDBB6M"));
    _ibor.put(
        "EURIBOR1M",
        new IborIndex(Currency.EUR, Period.ofMonths(1), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "EURIBOR1M"));
    _ibor.put(
        "EURIBOR3M",
        new IborIndex(Currency.EUR, Period.ofMonths(3), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "EURIBOR3M"));
    _ibor.put(
        "EURIBOR6M",
        new IborIndex(Currency.EUR, Period.ofMonths(6), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "EURIBOR6M"));
    _ibor.put(
        "EURIBOR12M",
        new IborIndex(Currency.EUR, Period.ofMonths(12), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "EURIBOR12M"));
    _ibor.put(
        "USDLIBOR1M",
        new IborIndex(Currency.EUR, Period.ofMonths(1), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "USDLIBOR1M"));
    _ibor.put(
        "USDLIBOR3M",
        new IborIndex(Currency.USD, Period.ofMonths(3), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "USDLIBOR3M"));
    _ibor.put(
        "USDLIBOR6M",
        new IborIndex(Currency.USD, Period.ofMonths(6), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "USDLIBOR6M"));
    _ibor.put(
        "USDLIBOR12M",
        new IborIndex(Currency.USD, Period.ofMonths(12), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "USDLIBOR12M"));
    _ibor.put(
        "GBPLIBOR3M",
        new IborIndex(Currency.GBP, Period.ofMonths(3), 0, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "GBPLIBOR3M"));
    _ibor.put(
        "GBPLIBOR6M",
        new IborIndex(Currency.GBP, Period.ofMonths(6), 0, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "GBPLIBOR6M"));
    _ibor.put(
        "DKKCIBOR3M",
        new IborIndex(Currency.DKK, Period.ofMonths(3), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "DKKCIBOR3M"));
    _ibor.put(
        "DKKCIBOR6M",
        new IborIndex(Currency.DKK, Period.ofMonths(6), 2, baseCalendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
            .getBusinessDayConvention("Modified Following"), true, "DKKCIBOR6M"));
  }

  public IborIndex getIndex(final String name, final Calendar cal) {
    IborIndex indexNoCalendar = _ibor.get(name);
    if (indexNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new IborIndex(indexNoCalendar.getCurrency(), indexNoCalendar.getTenor(), indexNoCalendar.getSpotLag(), cal, indexNoCalendar.getDayCount(), indexNoCalendar.getBusinessDayConvention(),
        indexNoCalendar.isEndOfMonth(), indexNoCalendar.getName());
  }

}
