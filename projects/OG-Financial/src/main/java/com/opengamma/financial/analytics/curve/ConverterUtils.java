/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

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
  
  /**
   * Create a IborIndex object from the convention and the tenor.
   * @param name The name of the index.
   * @param indexConvention The index convention.
   * @param indexTenor The index tenor.
   * @return The IborIndex object.
   */
  public static IborIndex indexIbor(final String name, final IborIndexConvention indexConvention, final Tenor indexTenor) {
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eomIndex = indexConvention.isIsEOM();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor.getPeriod(), spotLag, dayCount, businessDayConvention, eomIndex, name);
    return iborIndex;
  }
  
  /**
   * Create a IndexPrice object from the name and the convention.
   * @param name The name of the index.
   * @param indexConvention The index convention.
   * @return The IndexPrice object.
   */
  public static IndexPrice indexPrice(final String name, final PriceIndexConvention indexConvention) {
    final IndexPrice priceIndex = new IndexPrice(name, indexConvention.getCurrency());
    return priceIndex;
  }

}
