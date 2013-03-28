/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public final class PriceIndexMaster {

  /**
   * The method unique instance.
   */
  private static final PriceIndexMaster INSTANCE = new PriceIndexMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PriceIndexMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of Ibor Indexes and their conventions.
   */
  private final Map<String, PriceIndex> _priceIndex;

  /**
   * Private constructor.
   */
  private PriceIndexMaster() {

    _priceIndex = new HashMap<String, PriceIndex>();
    _priceIndex.put(
        "EURHICP",
        new PriceIndex("EUR HICP", Currency.EUR));
    _priceIndex.put(
        "UKRPI",
        new PriceIndex("UK RPI", Currency.EUR));
    _priceIndex.put(
        "FRCPI",
        new PriceIndex("FR CPI", Currency.EUR));
    _priceIndex.put(
        "USCPI",
        new PriceIndex("US CPI", Currency.USD));
  }

  public PriceIndex getIndex(final String name) {
    final PriceIndex indexNoCalendar = _priceIndex.get(name);
    if (indexNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new PriceIndex(indexNoCalendar.getName(), indexNoCalendar.getCurrency());
  }

}
