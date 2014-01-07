/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.SoybeanFutureExpiryCalculator;

/**
 *
 */
public class CommodityFuturePriceCurveFunction extends FuturePriceCurveFunction {

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.COMMODITY_FUTURE_PRICE;
  }

  @Override
  protected Double getTimeToMaturity(final int n, final LocalDate date, final Calendar calendar) {
    //TODO: need to get rules from instrument provider (so can handle different rules for different tickers)
    return TimeCalculator.getTimeBetween(date, SoybeanFutureExpiryCalculator.getInstance().getExpiryDate(n, date, calendar));
  }

}
