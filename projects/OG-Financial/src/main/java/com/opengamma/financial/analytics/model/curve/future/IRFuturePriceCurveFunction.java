/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 *
 */
public class IRFuturePriceCurveFunction extends FuturePriceCurveFunction {

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.IR_FUTURE_PRICE;
  }

  @Override
  // TODO: REMOVE - getTimeToMaturity is no longer used.
  protected Double getTimeToMaturity(final int n, final LocalDate date, final Calendar calendar) {
    final LocalDate nthExpiry = FutureOptionExpiries.IR.getExpiryDate(n, date, calendar);
    return TimeCalculator.getTimeBetween(date, nthExpiry);
  }

}
