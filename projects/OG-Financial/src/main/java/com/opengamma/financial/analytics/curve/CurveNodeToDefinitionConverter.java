/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveNodeToDefinitionConverter {
  private final ConventionSource _conventionSource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;

  public CurveNodeToDefinitionConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource) {
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  public InstrumentDefinition<?> getDefinitionForNode(final CurveNode node, final ExternalId marketDataId, final ZonedDateTime now, final SnapshotDataBundle marketValues) {
    final CurveNodeVisitor<InstrumentDefinition<?>> nodeVisitor = new CurveNodeVisitor<InstrumentDefinition<?>>() {

      @Override
      public InstrumentDefinition<?> visitCashNode(final CashNode cashNode) {
        final Convention convention = _conventionSource.getConvention(cashNode.getConvention());
        final Currency currency;
        final BusinessDayConvention businessDayConvention;
        final DayCount dayCount;
        final Calendar calendar;
        final boolean isEOM;
        int daysToSettle;
        if (convention instanceof DepositConvention) {
          final DepositConvention depositConvention = (DepositConvention) convention;
          currency = depositConvention.getCurrency();
          calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, depositConvention.getRegionCalendar());
          businessDayConvention = depositConvention.getBusinessDayConvention();
          isEOM = depositConvention.isIsEOM();
          dayCount = depositConvention.getDayCount();
          daysToSettle = depositConvention.getDaysToSettle();
        } else if (convention instanceof IborIndexConvention) {
          final IborIndexConvention iborConvention = (IborIndexConvention) convention;
          currency = iborConvention.getCurrency();
          calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, iborConvention.getRegionCalendar());
          businessDayConvention = iborConvention.getBusinessDayConvention();
          isEOM = iborConvention.isIsEOM();
          dayCount = iborConvention.getDayCount();
          daysToSettle = iborConvention.getDaysToSettle();
        } else {
          throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
        }
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(now, cashNode.getStartTenor().getPeriod().plusDays(daysToSettle), businessDayConvention, calendar);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, cashNode.getMaturityTenor().getPeriod(), businessDayConvention, calendar, isEOM);
        final double accrualFactor = dayCount.getDayCountFraction(now, endDate);
        final double rate = marketValues.getDataPoint(marketDataId);
        return new CashDefinition(currency, startDate, endDate, 1, rate, accrualFactor);
      }

      @Override
      public InstrumentDefinition<?> visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
        throw new UnsupportedOperationException();
      }

      @Override
      public InstrumentDefinition<?> visitCreditSpreadNode(final CreditSpreadNode node) {
        throw new UnsupportedOperationException();
      }

      @Override
      public InstrumentDefinition<?> visitDiscountFactorNode(final DiscountFactorNode node) {
        throw new UnsupportedOperationException();
      }

      @Override
      public InstrumentDefinition<?> visitFRANode(final FRANode node) {
        final double rate = marketValues.getDataPoint(marketDataId);
        final IborIndexConvention convention = _conventionSource.getIborIndexConvention(node.getConvention());
        return null;
        //return new ForwardRateAgreementDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, 1, fixingDate, index, rate, calendar);
      }

      @Override
      public InstrumentDefinition<?> visitRateFutureNode(final RateFutureNode node) {
        return null;
      }

      @Override
      public InstrumentDefinition<?> visitSwapNode(final SwapNode node) {
        return null;
      }
    };
    return node.accept(nodeVisitor);
  }

}
