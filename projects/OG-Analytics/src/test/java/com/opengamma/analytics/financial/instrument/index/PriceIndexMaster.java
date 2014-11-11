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
  private final Map<String, IndexPrice> _priceIndex;

  /**
   * Private constructor.
   */
  private PriceIndexMaster() {

    _priceIndex = new HashMap<>();
    _priceIndex.put("EURHICP", new IndexPrice("EUR HICP", Currency.EUR));
    _priceIndex.put(
        "UKRPI",
        new IndexPrice("UK RPI", Currency.GBP));
    _priceIndex.put(
        "FRCPI",
        new IndexPrice("FR CPI", Currency.EUR));
    _priceIndex.put(
        "USCPI",
        new IndexPrice("US CPI", Currency.USD));
    _priceIndex.put("BRIPCA", new IndexPrice("BR IPCA", Currency.BRL));
  }

  public IndexPrice getIndex(final String name) {
    final IndexPrice indexNoCalendar = _priceIndex.get(name);
    if (indexNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new IndexPrice(indexNoCalendar.getName(), indexNoCalendar.getCurrency());
  }

}
