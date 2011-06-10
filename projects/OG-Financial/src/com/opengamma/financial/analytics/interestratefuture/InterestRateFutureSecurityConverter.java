/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.analytics.fixedincome.CalendarUtil;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
//TODO rename me
public class InterestRateFutureSecurityConverter implements FutureSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public InterestRateFutureSecurityConverter(final HolidaySource holidaySource,
      final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitBondFutureSecurity(final BondFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitEquityFutureSecurity(final EquityFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitFXFutureSecurity(final FXFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitIndexFutureSecurity(final IndexFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    Validate.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Currency currency = security.getCurrency();
    final ConventionBundle iborConvention = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_IRFUTURE"));
    if (iborConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor convention for " + currency.getCode());
    }
    final Calendar calendar = CalendarUtil.getCalendar(_regionSource, _holidaySource, RegionUtils.currencyRegionId(currency)); //TODO exchange region?
    final double paymentAccrualFactor = iborConvention.getFutureYearFraction();
    final IborIndex iborIndex = new IborIndex(currency, getTenor(paymentAccrualFactor), iborConvention.getSettlementDays(),
        calendar, iborConvention.getDayCount(), iborConvention.getBusinessDayConvention(),
        iborConvention.isEOMConvention());
    final double notional = 1000000; //TODO don't have this hard-coded
    return new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, notional, paymentAccrualFactor);
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitMetalFutureSecurity(final MetalFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitStockFutureSecurity(final StockFutureSecurity security) {
    throw new NotImplementedException();
  }

  //TODO this should be not be done here - we probably need the period in the security
  private Period getTenor(final double accrualFactor) {
    if (Double.doubleToLongBits(accrualFactor) == Double.doubleToLongBits(0.25)) {
      return Period.ofMonths(3);
    }
    if (Double.doubleToLongBits(accrualFactor) == Double.doubleToLongBits(1. / 12)) {
      return Period.ofMonths(1);
    }
    throw new OpenGammaRuntimeException("Could not get period for an interest rate future with accrual factor " + accrualFactor);
  }
}
