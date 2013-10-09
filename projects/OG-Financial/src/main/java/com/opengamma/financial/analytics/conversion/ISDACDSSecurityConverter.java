/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Build the ISDA definition for a CDS security object
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see CDSSecurity
 * @see ISDACDSDefinition
 */
public class ISDACDSSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  private static final boolean ACCRUAL_ON_DEFAULT = true;
  private static final boolean PAY_ON_DEFAULT = true;
  private static final boolean PROTECT_START = true;

  private final HolidaySource _holidaySource;

  public ISDACDSSecurityConverter(final HolidaySource holidaySource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _holidaySource = holidaySource;
  }

  /**
   * Convert a CDS security in to an ISDA definition object
   *
   * @param cds The CDS security to convert
   * @return An ISDA definition object for the CDS, suitable for use by the ISDA pricer
   */
  @Override
  public InstrumentDefinition<?> visitCDSSecurity(final CDSSecurity cds) {

    // TODO: Does convention name matter? ISDA code never uses it
    final Calendar calendar = CalendarUtils.getCalendar(_holidaySource, cds.getCurrency());
    final Convention convention = new Convention(
      cds.getSettlementDays(), cds.getDayCount(), cds.getBusinessDayConvention(), calendar, cds.getName() + "_convention"); // TODO: Is convention name important?

    final ISDACDSPremiumDefinition premiumPayments = ISDACDSPremiumDefinition.from(
      cds.getStartDate(), cds.getMaturity(), cds.getPremiumFrequency(),
      convention, cds.getStubType(), PROTECT_START,
      cds.getNotional(), cds.getSpread(), cds.getCurrency(), calendar);

    return new ISDACDSDefinition(cds.getStartDate(), cds.getMaturity(), premiumPayments,
      cds.getNotional(), cds.getSpread(), cds.getRecoveryRate(), ACCRUAL_ON_DEFAULT,
      PAY_ON_DEFAULT, PROTECT_START, cds.getPremiumFrequency(), convention, cds.getStubType());
  }
}
