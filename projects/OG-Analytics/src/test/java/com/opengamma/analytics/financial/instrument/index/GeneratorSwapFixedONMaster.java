/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * A list of generators for swaps Fixed/ON available for tests.
 */
public final class GeneratorSwapFixedONMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapFixedONMaster INSTANCE = new GeneratorSwapFixedONMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorSwapFixedONMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwapFixedON> _generatorSwap;

  /**
   * Private constructor.
   */
  private GeneratorSwapFixedONMaster() {
    final IndexONMaster indexONMaster = IndexONMaster.getInstance();
    Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    BusinessDayConvention modFol = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    _generatorSwap = new HashMap<String, GeneratorSwapFixedON>();
    IndexON fedFund = indexONMaster.getIndex("FED FUND", baseCalendar);
    _generatorSwap.put("USD1YFEDFUND", new GeneratorSwapFixedON("USD1YFEDFUND", fedFund, Period.ofMonths(12), act360, modFol, true, 2, 2));
    _generatorSwap.put("EUR1YEONIA", new GeneratorSwapFixedON("EUR1YEONIA", indexONMaster.getIndex("EONIA", baseCalendar), Period.ofMonths(12), act360, modFol, true, 2, 2));
    _generatorSwap.put("AUD1YRBAON", new GeneratorSwapFixedON("AUD1YRBAON", indexONMaster.getIndex("RBA ON", baseCalendar), Period.ofMonths(12), act365, modFol, true, 2, 1));
    _generatorSwap.put("JPY1YTONAR", new GeneratorSwapFixedON("JPY1YTONAR", indexONMaster.getIndex("TONAR", baseCalendar), Period.ofMonths(12), act365, modFol, true, 2, 1));
  }

  public GeneratorSwapFixedON getGenerator(final String name, final Calendar cal) {
    GeneratorSwapFixedON generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Swap Fixed/ON generator for " + name);
    }
    IndexON indexNoCalendar = generatorNoCalendar.getIndex();
    IndexON index = new IndexON(indexNoCalendar.getName(), indexNoCalendar.getCurrency(), indexNoCalendar.getDayCount(), indexNoCalendar.getPublicationLag(), cal);
    return new GeneratorSwapFixedON(generatorNoCalendar.getName(), index, generatorNoCalendar.getLegsPeriod(), generatorNoCalendar.getFixedLegDayCount(),
        generatorNoCalendar.getBusinessDayConvention(), generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getSpotLag(), generatorNoCalendar.getPaymentLag());
  }

}
