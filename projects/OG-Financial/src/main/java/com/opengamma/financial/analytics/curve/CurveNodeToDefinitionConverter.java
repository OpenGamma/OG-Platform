/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Converts {@link CurveNode}s into {@InstrumentDefinition}s.
 */
public class CurveNodeToDefinitionConverter {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   */
  public CurveNodeToDefinitionConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  /**
   * Converts a {@link CurveNode} into an {@link InstrumentDefinition}, which will be used in curve construction.
   * @param node The curve node, not null
   * @param marketDataId The market data id, not null
   * @param now The curve construction time, not null
   * @param marketValues The market data values for these nodes, not null
   * @param timeSeries The historical time series needed for these nodes, not null
   * @param curveName The curve name, not null
   * @return An instrument definition for this node
   */
  public InstrumentDefinition<?> getDefinitionForNode(final CurveNode node, final ExternalId marketDataId, final ZonedDateTime now, final SnapshotDataBundle marketValues, //CSIGNORE
      final HistoricalTimeSeriesBundle timeSeries, final String curveName) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(now, "now");
    ArgumentChecker.notNull(marketValues, "market values");
    ArgumentChecker.notNull(timeSeries, "time series");
    ArgumentChecker.notNull(curveName, "curve name");
    final CurveNodeVisitor<InstrumentDefinition<?>> nodeVisitor = new CurveNodeVisitor<InstrumentDefinition<?>>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public InstrumentDefinition<?> visitCashNode(final CashNode cashNode) {
        final Convention convention = _conventionSource.getConvention(cashNode.getConvention());
        if (convention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + cashNode.getConvention() + " was null");
        }
        final Double rate = marketValues.getDataPoint(marketDataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
        }
        final Period startPeriod = cashNode.getStartTenor().getPeriod();
        final Period maturityPeriod = cashNode.getMaturityTenor().getPeriod();
        if (convention instanceof DepositConvention) {
          final DepositConvention depositConvention = (DepositConvention) convention;
          final Currency currency = depositConvention.getCurrency();
          final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, depositConvention.getRegionCalendar());
          final BusinessDayConvention businessDayConvention = depositConvention.getBusinessDayConvention();
          final boolean isEOM = depositConvention.isIsEOM();
          final DayCount dayCount = depositConvention.getDayCount();
          final int settlementDays = depositConvention.getSettlementDays();
          final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(now, settlementDays, calendar);
          final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar);
          final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
          final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
          return new CashDefinition(currency, startDate, endDate, 1, rate, accrualFactor);
        } else if (convention instanceof IborIndexConvention) {
          final IborIndexConvention iborConvention = (IborIndexConvention) convention;
          final Currency currency = iborConvention.getCurrency();
          final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, iborConvention.getRegionCalendar());
          final BusinessDayConvention businessDayConvention = iborConvention.getBusinessDayConvention();
          final boolean isEOM = iborConvention.isIsEOM();
          final DayCount dayCount = iborConvention.getDayCount();
          final int settlementDays = iborConvention.getSettlementDays();
          final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(now, startPeriod.plusDays(settlementDays), businessDayConvention, calendar);
          final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
          final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
          final int spotLag = 0; //TODO
          final boolean eom = iborConvention.isIsEOM();
          final long months = maturityPeriod.toTotalMonths() - startPeriod.toTotalMonths();
          final Period indexTenor = Period.ofMonths((int) months);
          final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, convention.getName());
          return new DepositIborDefinition(currency, startDate, endDate, 1, rate, accrualFactor, iborIndex);
        } else {
          throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
        }
      }

      @Override
      public InstrumentDefinition<?> visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode continuouslyCompoundedNode) {
        throw new UnsupportedOperationException();
      }

      @Override
      public InstrumentDefinition<?> visitCreditSpreadNode(final CreditSpreadNode creditSpreadNode) {
        throw new UnsupportedOperationException();
      }

      @Override
      public InstrumentDefinition<?> visitDiscountFactorNode(final DiscountFactorNode discountFactorNode) {
        throw new UnsupportedOperationException();
      }

      //TODO check calendars
      @SuppressWarnings("synthetic-access")
      @Override
      public InstrumentDefinition<?> visitFRANode(final FRANode fraNode) {
        final Double rate = marketValues.getDataPoint(marketDataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
        }
        final Convention convention = _conventionSource.getConvention(fraNode.getConvention());
        final Period startPeriod = fraNode.getFixingStart().getPeriod();
        final Period endPeriod = fraNode.getFixingEnd().getPeriod();
        //TODO probably need a specific FRA convention to hold the reset tenor
        final long months = endPeriod.toTotalMonths() - startPeriod.toTotalMonths();
        final Period indexTenor = Period.ofMonths((int) months);
        final IborIndexConvention indexConvention;
        if (convention instanceof IborIndexConvention) {
          indexConvention = (IborIndexConvention) convention;
        } else {
          if (convention == null) {
            throw new OpenGammaRuntimeException("Convention with id " + fraNode.getConvention() + " was null");
          }
          throw new OpenGammaRuntimeException("Could not handle underlying convention of type " + convention.getClass());
        }
        final Currency currency = indexConvention.getCurrency();
        final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
        final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
        final int settlementDays = indexConvention.getSettlementDays();
        final int spotLag = 0; //TODO
        final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
        final DayCount dayCount = indexConvention.getDayCount();
        final boolean eom = indexConvention.isIsEOM();
        final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, convention.getName());
        final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(now, settlementDays, regionCalendar);
        final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, regionCalendar, eom);
        final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(now, endPeriod, businessDayConvention, regionCalendar, eom);
        return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, 1, iborIndex, rate, fixingCalendar);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public InstrumentDefinition<?> visitFXForwardNode(final FXForwardNode fxForward) {
        final Double forwardRate = marketValues.getDataPoint(marketDataId);
        if (forwardRate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
        }
        final ExternalId conventionId = fxForward.getFxForwardConvention();
        final Convention convention = _conventionSource.getConvention(conventionId);
        if (convention == null) {
          throw new OpenGammaRuntimeException("Could not get convention with id " + conventionId);
        }
        if (!(convention instanceof FXForwardAndSwapConvention)) {
          throw new OpenGammaRuntimeException("Need a convention of type " + FXForwardAndSwapConvention.class + ", have " + convention.getClass());
        }
        final FXForwardAndSwapConvention forwardConvention = (FXForwardAndSwapConvention) convention;
        final ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
        final Convention underlyingConvention = _conventionSource.getConvention(underlyingConventionId);
        if (underlyingConvention == null) {
          throw new OpenGammaRuntimeException("Could not get convention with id " + underlyingConventionId);
        }
        if (!(underlyingConvention instanceof FXSpotConvention)) {
          throw new OpenGammaRuntimeException("Need a convention of type " + FXSpotConvention.class + ", have " + convention.getClass());
        }
        final FXSpotConvention spotConvention = (FXSpotConvention) underlyingConvention;
        final Currency payCurrency = fxForward.getPayCurrency();
        final Currency receiveCurrency = fxForward.getReceiveCurrency();
        final Tenor forwardTenor = fxForward.getMaturityTenor();
        final double payAmount = 1;
        final double receiveAmount = forwardRate;
        final int settlementDays = spotConvention.getSettlementDays();
        final ExternalId settlementRegion = forwardConvention.getSettlementRegion();
        final Calendar settlementCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, settlementRegion);
        final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(now, settlementDays, settlementCalendar);
        final ZonedDateTime exchangeDate = ScheduleCalculator.getAdjustedDate(spotDate, forwardTenor.getPeriod(), forwardConvention.getBusinessDayConvention(), settlementCalendar,
            forwardConvention.isIsEOM());
        return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, exchangeDate, payAmount, -receiveAmount);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public InstrumentDefinition<?> visitRateFutureNode(final RateFutureNode rateFuture) {
        final Double rate = marketValues.getDataPoint(marketDataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
        }
        final Convention futureConvention = _conventionSource.getConvention(rateFuture.getFutureConvention());
        final String expiryCalculatorName;
        if (futureConvention instanceof InterestRateFutureConvention) {
          expiryCalculatorName = ((InterestRateFutureConvention) futureConvention).getExpiryConvention().getValue();
        } else {
          if (futureConvention == null) {
            throw new OpenGammaRuntimeException("Future convention was null");
          }
          throw new OpenGammaRuntimeException("Could not handle future convention of type " + futureConvention.getClass());
        }
        final Convention underlyingConvention = _conventionSource.getConvention(rateFuture.getUnderlyingConvention());
        final IborIndexConvention indexConvention;
        final Period indexTenor = rateFuture.getUnderlyingTenor().getPeriod();
        if (underlyingConvention instanceof IborIndexConvention) {
          indexConvention = (IborIndexConvention) underlyingConvention;
        } else {
          if (underlyingConvention == null) {
            throw new OpenGammaRuntimeException("Underlying convention was null");
          }
          throw new OpenGammaRuntimeException("Could not handle underlying convention of type " + underlyingConvention.getClass());
        }
        final double paymentAccrualFactor = indexTenor.toTotalMonths() / 12.; //TODO don't use this method
        final Currency currency = indexConvention.getCurrency();
        final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
        final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
        final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
        final DayCount dayCount = indexConvention.getDayCount();
        final boolean eom = indexConvention.isIsEOM();
        final IborIndex iborIndex = new IborIndex(currency, indexTenor, 0, dayCount, businessDayConvention, eom, indexConvention.getName());
        final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
        final ZonedDateTime startDate = now.plus(rateFuture.getStartTenor().getPeriod());
        final LocalTime time = startDate.toLocalTime();
        final ZoneId timeZone = startDate.getZone();
        final ZonedDateTime lastTradeDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(), startDate.toLocalDate(), regionCalendar), time, timeZone);
        final InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, 1, paymentAccrualFactor, "", fixingCalendar);
        final InterestRateFutureTransactionDefinition transactionDefinition = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1);
        return transactionDefinition.withNewNotionalAndTransactionPrice(1, rate);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public InstrumentDefinition<?> visitSwapNode(final SwapNode swapNode) {
        final Convention payLegConvention = _conventionSource.getConvention(swapNode.getPayLegConvention());
        if (payLegConvention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
        }
        final Convention receiveLegConvention = _conventionSource.getConvention(swapNode.getReceiveLegConvention());
        if (receiveLegConvention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
        }
        final AnnuityDefinition<? extends PaymentDefinition> payLeg;
        final AnnuityDefinition<? extends PaymentDefinition> receiveLeg;
        if (payLegConvention instanceof SwapFixedLegConvention) {
          final Double rate = marketValues.getDataPoint(marketDataId);
          if (rate == null) {
            throw new OpenGammaRuntimeException("Could not get fixed rate for swap");
          }
          payLeg = getFixedLeg((SwapFixedLegConvention) payLegConvention, swapNode, rate, true);
        } else if (payLegConvention instanceof VanillaIborLegConvention) {
          payLeg = getIborLeg((VanillaIborLegConvention) payLegConvention, swapNode, true);
        } else if (payLegConvention instanceof OISLegConvention) {
          payLeg = getOISLeg((OISLegConvention) payLegConvention, swapNode, true);
        } else {
          throw new OpenGammaRuntimeException("Cannot handle convention type " + payLegConvention.getClass());
        }
        if (receiveLegConvention instanceof SwapFixedLegConvention) {
          final Double rate = marketValues.getDataPoint(marketDataId);
          if (rate == null) {
            throw new OpenGammaRuntimeException("Could not get fixed rate for swap");
          }
          receiveLeg = getFixedLeg((SwapFixedLegConvention) receiveLegConvention, swapNode, rate, false);
        } else if (receiveLegConvention instanceof VanillaIborLegConvention) {
          receiveLeg = getIborLeg((VanillaIborLegConvention) receiveLegConvention, swapNode, false);
        } else if (receiveLegConvention instanceof OISLegConvention) {
          receiveLeg = getOISLeg((OISLegConvention) receiveLegConvention, swapNode, false);
        } else {
          throw new OpenGammaRuntimeException("Cannot handle convention type " + receiveLegConvention.getClass());
        }
        return new SwapDefinition(payLeg, receiveLeg);
      }

      @SuppressWarnings({"synthetic-access" })
      @Override
      public InstrumentDefinition<?> visitZeroCouponInflationNode(final ZeroCouponInflationNode inflationNode) {
        Convention convention = _conventionSource.getConvention(inflationNode.getFixedLegConvention());
        if (convention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + inflationNode.getFixedLegConvention() + " was null");
        }
        if (!(convention instanceof SwapFixedLegConvention)) {
          throw new OpenGammaRuntimeException("Cannot handle convention type " + convention.getClass());
        }
        final SwapFixedLegConvention fixedLegConvention = (SwapFixedLegConvention) convention;
        convention = _conventionSource.getConvention(inflationNode.getInflationLegConvention());
        if (convention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + inflationNode.getInflationLegConvention() + " was null");
        }
        if (!(convention instanceof InflationLegConvention)) {
          throw new OpenGammaRuntimeException("Cannot handle convention type " + convention.getClass());
        }
        final InflationLegConvention inflationLegConvention = (InflationLegConvention) convention;
        final Double rate = marketValues.getDataPoint(marketDataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
        }
        convention = _conventionSource.getConvention(inflationLegConvention.getPriceIndexConvention());
        if (convention == null) {
          throw new OpenGammaRuntimeException("Convention with id " + inflationLegConvention.getPriceIndexConvention() + " was null");
        }
        if (!(convention instanceof PriceIndexConvention)) {
          throw new OpenGammaRuntimeException("Cannot handle convention type " + convention.getClass());
        }
        final PriceIndexConvention priceIndexConvention = (PriceIndexConvention) convention;
        final int settlementDays = fixedLegConvention.getSettlementDays();
        final Period tenor = inflationNode.getTenor().getPeriod();
        final double notional = 1;
        //TODO business day convention and currency are in both conventions - should we enforce that they're the same or use
        // different ones for each leg?
        final BusinessDayConvention businessDayConvention = fixedLegConvention.getBusinessDayConvention();
        final boolean endOfMonth = fixedLegConvention.isIsEOM();
        final Currency currency = priceIndexConvention.getCurrency();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, priceIndexConvention.getRegion());
        final ZoneId zone = now.getZone(); //TODO time zone set to midnight UTC
        final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(now, settlementDays, calendar).toLocalDate().atStartOfDay(zone);
        final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, calendar, endOfMonth).toLocalDate().atStartOfDay(zone);
        final CouponFixedCompoundingDefinition fixedCoupon = CouponFixedCompoundingDefinition.from(currency, settlementDate, paymentDate, notional, tenor,
            rate);
        final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, priceIndexConvention.getPriceIndexId());
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price index time series with id " + priceIndexConvention.getPriceIndexId());
        }
        final LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
        final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries = convertTimeSeries(zone, localDateTS);
        final int conventionalMonthLag = inflationLegConvention.getMonthLag();
        final int monthLag = inflationLegConvention.getSpotLag();
        final IndexPrice index = new IndexPrice(curveName, currency);
        switch (inflationNode.getInflationNodeType()) {
          case INTERPOLATED:
          {
            final CouponInflationZeroCouponInterpolationDefinition inflationCoupon = CouponInflationZeroCouponInterpolationDefinition.from(settlementDate, paymentDate,
                -notional, index, priceIndexTimeSeries, conventionalMonthLag, monthLag, false);
            return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon);
          }
          case MONTHLY:
          {
            final CouponInflationZeroCouponMonthlyDefinition inflationCoupon = CouponInflationZeroCouponMonthlyDefinition.from(settlementDate, paymentDate, -notional,
                index, priceIndexTimeSeries, conventionalMonthLag, monthLag, false);
            return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon);
          }
          default:
            throw new OpenGammaRuntimeException("Could not handle inflation nodes of type " + inflationNode.getInflationNodeType());
        }
      }

      @SuppressWarnings("synthetic-access")
      private AnnuityCouponFixedDefinition getFixedLeg(final SwapFixedLegConvention convention, final SwapNode swapNode, final double rate, final boolean isPayer) {
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getRegionCalendar());
        final Currency currency = convention.getCurrency();
        final DayCount dayCount = convention.getDayCount();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final boolean eom = convention.isIsEOM();
        final int settlementDays = convention.getSettlementDays();
        final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(now.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
        return AnnuityCouponFixedDefinition.from(currency, settlementDate, maturityTenor, paymentPeriod, calendar, dayCount, businessDayConvention, eom, 1, rate, isPayer);
      }

      //TODO do we actually need the settlement days for the swap, not the index?
      @SuppressWarnings("synthetic-access")
      private AnnuityCouponIborDefinition getIborLeg(final VanillaIborLegConvention convention, final SwapNode swapNode, final boolean isPayer) {
        final Convention underlyingConvention = _conventionSource.getConvention(convention.getIborIndexConvention());
        if (!(underlyingConvention instanceof IborIndexConvention)) {
          if (underlyingConvention == null) {
            throw new OpenGammaRuntimeException("Could not get convention with id " + convention.getIborIndexConvention());
          }
          throw new OpenGammaRuntimeException("Convention of the underlying was not an ibor index convention; have " + underlyingConvention.getClass());
        }
        final IborIndexConvention indexConvention = (IborIndexConvention) underlyingConvention;
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
        final boolean eom = indexConvention.isIsEOM();
        final Period indexTenor = convention.getResetTenor().getPeriod();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
        final int spotLag = 0; //TODO
        final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, indexConvention.getName());
        final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
        final int settlementDays = convention.getSettlementDays();
        final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(now.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
        return AnnuityCouponIborDefinition.from(settlementDate, maturityTenor, 1, iborIndex, isPayer, calendar);
      }

      @SuppressWarnings("synthetic-access")
      private AnnuityCouponONSimplifiedDefinition getOISLeg(final OISLegConvention convention, final SwapNode swapNode, final boolean isPayer) {
        final OvernightIndexConvention indexConvention = (OvernightIndexConvention) _conventionSource.getConvention(convention.getOvernightIndexConvention());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final int publicationLag = indexConvention.getPublicationLag();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
        final int settlementDays = convention.getSettlementDays();
        final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
        final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean isEOM = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(now.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
        return AnnuityCouponONSimplifiedDefinition.from(settlementDate, maturityTenor, 1, isPayer, indexON, paymentLag, calendar, businessDayConvention,
            paymentPeriod, isEOM);
      }

      private ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
        // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
        final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
        for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext(); ) {
          final LocalDate date = it.nextTime();
          final ZonedDateTime zdt = date.atStartOfDay(timeZone);
          bld.put(zdt, it.currentValueFast());
        }
        return bld.build();
      }
    };
    return node.accept(nodeVisitor);
  }

}
