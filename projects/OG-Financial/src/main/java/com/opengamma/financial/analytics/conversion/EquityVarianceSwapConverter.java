/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link EquityVarianceSwapSecurity} to {@link EquityVarianceSwapDefinition}, which is required
 * for use in the analytics library.
 */
public class EquityVarianceSwapConverter extends FinancialSecurityVisitorAdapter<EquityVarianceSwapDefinition> {
  /** The holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param holidaySource Source for good business day information, not null
   */
  public EquityVarianceSwapConverter(final HolidaySource holidaySource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _holidaySource = holidaySource;
  }

  @Override
  public EquityVarianceSwapDefinition visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    final Currency currency = security.getCurrency();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, currency);   
    if (security.isParameterizedAsVariance()) {
      return EquityVarianceSwapDefinition.fromVarianceParams(security.getFirstObservationDate(), security.getLastObservationDate(), security.getSettlementDate(), security.getCurrency(), calendar,
          security.getAnnualizationFactor(), security.getStrike(), security.getNotional(), true);
    }
    return EquityVarianceSwapDefinition.fromVegaParams(security.getFirstObservationDate(), security.getLastObservationDate(), security.getSettlementDate(), security.getCurrency(), calendar,
        security.getAnnualizationFactor(), security.getStrike(), security.getNotional(), true);
  }

}
