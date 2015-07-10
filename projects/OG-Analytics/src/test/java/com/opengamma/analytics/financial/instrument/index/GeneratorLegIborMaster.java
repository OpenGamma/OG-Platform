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
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.util.money.Currency;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorLegIborMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorLegIborMaster INSTANCE = new GeneratorLegIborMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorLegIborMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorLegIbor> _generatorSwap;

  /**
   * The list of Ibor indexes for test purposes.
   */
  private final IndexIborMaster _iborIndexMaster;

  /**
   * Private constructor.
   */
  private GeneratorLegIborMaster() {
    _iborIndexMaster = IndexIborMaster.getInstance();
    final Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _generatorSwap = new HashMap<>();
    IborIndex usdlibor3M = _iborIndexMaster.getIndex("USDLIBOR3M");
    IborIndex usdlibor6M = _iborIndexMaster.getIndex("USDLIBOR6M");
    IborIndex eurEuribor3M = _iborIndexMaster.getIndex("EURIBOR3M");
    IborIndex eurEuribor6M = _iborIndexMaster.getIndex("EURIBOR6M");
    _generatorSwap.put("USDLIBOR3M", new GeneratorLegIbor("USDLIBOR3M", Currency.USD, usdlibor3M, Period.ofMonths(3), 
        2, 0, usdlibor3M.getBusinessDayConvention(), true, StubType.SHORT_START, false, baseCalendar, baseCalendar));
    _generatorSwap.put("USDLIBOR3M_X", new GeneratorLegIbor("USDLIBOR3M_X", Currency.USD, usdlibor3M, Period.ofMonths(3), 
        2, 0, usdlibor3M.getBusinessDayConvention(), true, StubType.SHORT_START, true, baseCalendar, baseCalendar));
    _generatorSwap.put("USDLIBOR6M", new GeneratorLegIbor("USDLIBOR6M", Currency.USD, usdlibor6M, Period.ofMonths(6), 
        2, 0, usdlibor6M.getBusinessDayConvention(), true, StubType.SHORT_START, false, baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR3M", new GeneratorLegIbor("EUREURIBOR3M", Currency.EUR, eurEuribor3M, 
        Period.ofMonths(3), 2, 0, eurEuribor3M.getBusinessDayConvention(), true, StubType.SHORT_START, false, 
        baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR3M_X", new GeneratorLegIbor("EUREURIBOR3M_X", Currency.EUR, eurEuribor3M, 
        Period.ofMonths(3), 2, 0, eurEuribor3M.getBusinessDayConvention(), true, StubType.SHORT_START, true, 
        baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR6M", new GeneratorLegIbor("EUREURIBOR6M", Currency.EUR, eurEuribor6M, 
        Period.ofMonths(6), 2, 0, eurEuribor6M.getBusinessDayConvention(), true, StubType.SHORT_START, false, 
        baseCalendar, baseCalendar));
  }

  public GeneratorLegIbor getGenerator(final String name, final Calendar cal) {
    final GeneratorLegIbor generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorLegIbor(generatorNoCalendar.getName(), generatorNoCalendar.getCurrency(), 
        generatorNoCalendar.getIndexIbor(), generatorNoCalendar.getPaymentPeriod(), generatorNoCalendar.getSpotOffset(), 
        generatorNoCalendar.getPaymentOffset(), generatorNoCalendar.getBusinessDayConvention(), 
        generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getStubType(), generatorNoCalendar.isExchangeNotional(), 
        cal, cal);
  }

}
