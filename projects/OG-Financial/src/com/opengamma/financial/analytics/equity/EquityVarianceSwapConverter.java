/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.equity;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.equity.variance.definition.VarianceSwapDefinition;
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
   * Converts an EquityVarianceSwapSecurity to an VarianceSwapDefinition
   * @param security The security
   * @return VarianceSwapDefinition
   */
  public VarianceSwapDefinition visitEquityVarianceSwapTrade(final EquityVarianceSwapSecurity security) {
    final Calendar calendar = CalendarUtils.getCalendar(_holidaySource, security.getCurrency()); // TODO CASE - Review. Holidays currently specified by currency alone
    final Frequency frequency = security.getObservationFrequency();
    final PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
    }
    if (security.isParameterizedAsVariance()) {
      return VarianceSwapDefinition.fromVarianceParams(security.getFirstObservationDate(), security.getLastObservationDate(),
                                          security.getSettlementDate(), periodFrequency,
                                          security.getCurrency(), calendar, security.getAnnualizationFactor(),
                                          security.getStrike(), security.getNotional());

    } 
    return VarianceSwapDefinition.fromVegaParams(security.getFirstObservationDate(), security.getLastObservationDate(),
                                        security.getSettlementDate(), periodFrequency,
                                        security.getCurrency(), calendar, security.getAnnualizationFactor(),
                                        security.getStrike(), security.getNotional());    
  }
  
  /**
   * Converts an EquityVarianceSwapSecurity Trade to an VarianceSwapDefinition
   * @param trade The trade
   * @return VarianceSwapDefinition
   */
  public VarianceSwapDefinition visitEquityVarianceSwapTrade(final SimpleTrade trade) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) trade.getSecurity();
    return visitEquityVarianceSwapTrade(security);
  }
}
