/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.equity;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.financial.analytics.conversion.CalendarUtil;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.equity.varswap.definition.VarianceSwapDefinition;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;

/**
 * 
 */
public class EquityVarianceSwapConverter {
  private final HolidaySource _holidaySource;

  /**
   * This constructor will construct calendar based upon currency alone.
   * @param holidaySource Source for good business day information
   */
  public EquityVarianceSwapConverter(HolidaySource holidaySource) {
    Validate.notNull(holidaySource, "holiday source");
    _holidaySource = holidaySource;
  }

  /**
   * Converts an EquityVarianceSwapSecurity Trade to an VarianceSwapDefinition
   * @param trade The trade
   * @return VarianceSwapDefinition
   */
  public VarianceSwapDefinition visitEquityVarianceSwapTrade(final TradeImpl trade) {

    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) trade.getSecurity();

    final Calendar calendar = CalendarUtil.getCalendar(_holidaySource, security.getCurrency()); // TODO CASE - Review. Holidays currently specified by currency alone

    if (security.getParameterisedAsVariance()) {
      return VarianceSwapDefinition.fromVarianceParams(security.getFirstObservationDate(), security.getLastObservationDate(),
                                          security.getSettlementDate(), (PeriodFrequency) security.getObservationFrequency(),
                                          security.getCurrency(), calendar, security.getAnnualizationFactor(),
                                          security.getStrike(), security.getNotional());

    } else {
      return VarianceSwapDefinition.fromVegaParams(security.getFirstObservationDate(), security.getLastObservationDate(),
                                          security.getSettlementDate(), (PeriodFrequency) security.getObservationFrequency(),
                                          security.getCurrency(), calendar, security.getAnnualizationFactor(),
                                          security.getStrike(), security.getNotional());
    }

  }
}
