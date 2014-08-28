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
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.util.money.Currency;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorLegOnAaMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorLegOnAaMaster INSTANCE = new GeneratorLegOnAaMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorLegOnAaMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorLegONArithmeticAverageAbstract> _generatorLeg;

  /**
   * Private constructor.
   */
  private GeneratorLegOnAaMaster() {
    final IndexONMaster indexONMaster = IndexONMaster.getInstance();
    final Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _generatorLeg = new HashMap<>();
    IndexON fedFund = indexONMaster.getIndex("FED FUND");
    _generatorLeg.put("USDFEDFUNDAA3M", new GeneratorLegONArithmeticAverage("USDFEDFUNDAA3M", Currency.USD, fedFund, 
        Period.ofMonths(3), 2, 0, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, 
        baseCalendar, baseCalendar));
  }

  public GeneratorLegONArithmeticAverageAbstract getGenerator(final String name, final Calendar cal) {
    final GeneratorLegONArithmeticAverageAbstract generatorNoCalendar = _generatorLeg.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorLegONArithmeticAverage(generatorNoCalendar.getName(), generatorNoCalendar.getCcy(), 
        generatorNoCalendar.getIndexON(), generatorNoCalendar.getPaymentPeriod(), generatorNoCalendar.getSpotOffset(), 
        generatorNoCalendar.getPaymentOffset(), generatorNoCalendar.getBusinessDayConvention(), 
        generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getStubType(), generatorNoCalendar.isExchangeNotional(), 
        cal, cal);
  }

}
