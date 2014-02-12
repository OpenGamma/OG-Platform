/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Utilities for different converters (Node and securities)/
 */
public class ConverterUtils {
  
  /**
   * Create an IndexON from the index name and the overnight index convention.
   * @param name The name of the index.
   * @param indexConvention The overnight index convention.
   * @return The IndexON object.
   */
  public static IndexON indexON(final String name, final OvernightIndexConvention indexConvention) {
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final IndexON indexON = new IndexON(name, currency, dayCount, publicationLag);
    return indexON;
  }

}
