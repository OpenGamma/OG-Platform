/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondSecurityToBondForwardDefinitionConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final BondSecurityToBondDefinitionConverter _converter;

  public BondSecurityToBondForwardDefinitionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _converter = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource);
  }

  public BondForwardDefinition getBondForward(final BondSecurity security, final ZonedDateTime deliveryDate) {
    Validate.notNull(security, "security");
    Validate.notNull(deliveryDate, "deliveryDate");
    final LocalDate deliveryDateLD = deliveryDate.toLocalDate();
    final LocalDate lastTradeDate = security.getLastTradeDate().getExpiry().toLocalDate();
    Validate.isTrue(deliveryDateLD.isBefore(lastTradeDate), "The bond has expired before delivery");
    //TODO bond futures are exchange-traded - check that this is the same calendar for the exchange as the currency
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    final Currency currency = security.getCurrency();
    final String conventionName = currency + "_BOND_FUTURE_DELIVERABLE_CONVENTION";
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName);
    final ConventionBundle conventionBundle = _conventionSource.getConventionBundle(id);
    Validate.notNull(conventionBundle, "convention bundle " + conventionName);
    final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    final BondDefinition underlyingBond = _converter.getBond(security, false, conventionBundle); //TODO use notional
    final BondConvention bondForwardConvention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar,
        conventionBundle.isEOMConvention(), conventionName, conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    return new BondForwardDefinition(underlyingBond, deliveryDate.toLocalDate(), bondForwardConvention);
  }
}
