/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class IborIndexConverter {
  private final ConventionSource _conventionSource;
  private final RegionSource _regionSource;
  private final HolidaySource _holidaySource;
  private final FinancialSecurityVisitor<IborIndex> _visitor = new MyVisitor();

  public IborIndexConverter(final ConventionSource conventionSource, final RegionSource regionSource, final HolidaySource holidaySource) {
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _holidaySource = holidaySource;
  }

  public IborIndex getConvention(final FinancialSecurity security, final ConventionSource conventionSource) {
    return security.accept(_visitor);
  }

  private class MyVisitor extends FinancialSecurityVisitorAdapter<IborIndex> {

    public MyVisitor() {
    }

    @Override
    public IborIndex visitFRASecurity(final FRASecurity security) {
      final Currency currency = security.getCurrency();
      final String conventionName = PerCurrencyConventionHelper.getConventionName(currency, PerCurrencyConventionHelper.VANILLA_IBOR_LEG);
      final IborIndexConvention convention = _conventionSource.getIborIndexConvention(PerCurrencyConventionHelper.simpleNameId(null));
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getRegionCalendar());
      return new IborIndex(currency, convention.getTenor().getPeriod(), convention.getDaysToSettle(), calendar, convention.getDayCount(),
          convention.getBusinessDayConvention(), convention.isIsEOM());
    }

    @Override
    public IborIndex visitSwapSecurity(final SwapSecurity security) {
      return null;
      //      return new IborIndex(currency, convention.getTenor().getPeriod(), convention.getDaysToSettle(), calendar, convention.getDayCount(),
      //          convention.getBusinessDayConvention(), convention.isIsEOM());
    }
  }
}
