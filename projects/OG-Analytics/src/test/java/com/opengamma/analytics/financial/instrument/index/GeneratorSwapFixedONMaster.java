/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

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
    final Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365;
    final BusinessDayConvention modFol = BusinessDayConventions.MODIFIED_FOLLOWING;
    _generatorSwap = new HashMap<>();
    final IndexON fedFund = indexONMaster.getIndex("FED FUND");
    _generatorSwap.put("USD1YFEDFUND", new GeneratorSwapFixedON("USD1YFEDFUND", fedFund, Period.ofMonths(12), act360, modFol, true, 2, 2, baseCalendar));
    _generatorSwap.put("EUR1YEONIA", new GeneratorSwapFixedON("EUR1YEONIA", indexONMaster.getIndex("EONIA"), Period.ofMonths(12), act360, modFol, true, 2, 2, baseCalendar));
    _generatorSwap.put("AUD1YRBAON", new GeneratorSwapFixedON("AUD1YRBAON", indexONMaster.getIndex("RBA ON"), Period.ofMonths(12), act365, modFol, true, 2, 1, baseCalendar));
    _generatorSwap.put("JPY1YTONAR", new GeneratorSwapFixedON("JPY1YTONAR", indexONMaster.getIndex("TONAR"), Period.ofMonths(12), act365, modFol, true, 2, 1, baseCalendar));
  }

  public GeneratorSwapFixedON getGenerator(final String name, final Calendar cal) {
    final GeneratorSwapFixedON generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Swap Fixed/ON generator for " + name);
    }
    final IndexON indexNoCalendar = generatorNoCalendar.getIndex();
    final IndexON index = new IndexON(indexNoCalendar.getName(), indexNoCalendar.getCurrency(), indexNoCalendar.getDayCount(), indexNoCalendar.getPublicationLag());
    return new GeneratorSwapFixedON(generatorNoCalendar.getName(), index, generatorNoCalendar.getLegsPeriod(), generatorNoCalendar.getFixedLegDayCount(),
        generatorNoCalendar.getBusinessDayConvention(), generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getSpotLag(), generatorNoCalendar.getPaymentLag(), cal);
  }

}
