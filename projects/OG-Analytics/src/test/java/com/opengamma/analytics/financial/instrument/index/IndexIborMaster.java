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
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Description of Ibor indexes available for tests.
 */
public final class IndexIborMaster {

  /**
   * The method unique instance.
   */
  private static final IndexIborMaster INSTANCE = new IndexIborMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static IndexIborMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of Ibor Indexes and their conventions.
   */
  private final Map<String, IborIndex> _ibor;

  /**
   * Private constructor.
   */
  private IndexIborMaster() {
    _ibor = new HashMap<>();
    _ibor.put(
        "AUDBB3M",
        new IborIndex(Currency.AUD, Period.ofMonths(3), 1, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "AUDBB3M"));
    _ibor.put(
        "AUDBB6M",
        new IborIndex(Currency.AUD, Period.ofMonths(6), 1, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "AUDBB6M"));
    _ibor.put(
        "CADCDOR3M",
        new IborIndex(Currency.CAD, Period.ofMonths(3), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "CADCDOR3M"));
    _ibor.put(
        "EURIBOR1M",
        new IborIndex(Currency.EUR, Period.ofMonths(1), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "EURIBOR1M"));
    _ibor.put(
        "EURIBOR3M",
        new IborIndex(Currency.EUR, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "EURIBOR3M"));
    _ibor.put(
        "EURIBOR6M",
        new IborIndex(Currency.EUR, Period.ofMonths(6), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "EURIBOR6M"));
    _ibor.put(
        "EURIBOR12M",
        new IborIndex(Currency.EUR, Period.ofMonths(12), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "EURIBOR12M"));
    _ibor.put(
        "USDLIBOR1M",
        new IborIndex(Currency.USD, Period.ofMonths(1), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "USDLIBOR1M"));
    _ibor.put(
        "USDLIBOR3M",
        new IborIndex(Currency.USD, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "USDLIBOR3M"));
    _ibor.put(
        "USDLIBOR6M",
        new IborIndex(Currency.USD, Period.ofMonths(6), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "USDLIBOR6M"));
    _ibor.put(
        "USDLIBOR12M",
        new IborIndex(Currency.USD, Period.ofMonths(12), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "USDLIBOR12M"));
    _ibor.put(
        "GBPLIBOR3M",
        new IborIndex(Currency.GBP, Period.ofMonths(3), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "GBPLIBOR3M"));
    _ibor.put(
        "GBPLIBOR6M",
        new IborIndex(Currency.GBP, Period.ofMonths(6), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "GBPLIBOR6M"));
    _ibor.put(
        "DKKCIBOR3M",
        new IborIndex(Currency.DKK, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "DKKCIBOR3M"));
    _ibor.put(
        "DKKCIBOR6M",
        new IborIndex(Currency.DKK, Period.ofMonths(6), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "DKKCIBOR6M"));
    _ibor.put(
        "JPYLIBOR3M",
        new IborIndex(Currency.JPY, Period.ofMonths(3), 2, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "JPYLIBOR3M"));
    _ibor.put(
        "JPYLIBOR6M",
        new IborIndex(Currency.JPY, Period.ofMonths(6), 2, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            true, "JPYLIBOR6M"));
  }

  public IborIndex getIndex(final String name) {
    final IborIndex indexNoCalendar = _ibor.get(name);
    if (indexNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new IborIndex(indexNoCalendar.getCurrency(), indexNoCalendar.getTenor(), indexNoCalendar.getSpotLag(), indexNoCalendar.getDayCount(), indexNoCalendar.getBusinessDayConvention(),
        indexNoCalendar.isEndOfMonth(), indexNoCalendar.getName());
  }

}
