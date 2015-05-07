/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.ArrayList;
import java.util.Collection;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Function implementation that provides a historical time-series bundle.
 */
public class DefaultFixingsFn implements FixingsFn {

  private static final HistoricalTimeSeriesBundle EMPTY_TIME_SERIES_BUNDLE = new HistoricalTimeSeriesBundle();
  private static final Period ONE_MONTH = Period.ofMonths(1);

  private final HistoricalMarketDataFn _historicalMarketDataFn;

  public DefaultFixingsFn(HistoricalMarketDataFn historicalMarketDataFn) {
    _historicalMarketDataFn = ArgumentChecker.notNull(historicalMarketDataFn, "historicalMarketDataFn");
  }

  @Override
  public Result<HistoricalTimeSeriesBundle> getFixingsForSecurity(Environment env, FinancialSecurity security) {
    FixingRetriever retriever = new FixingRetriever(env, env.getValuationDate());
    try {
      return security.accept(retriever);
    } catch (Exception ex) {
      return Result.failure(ex);
    }
  }

  /**
   * Class that returns a timeseries bundle of the fixing timeseries required by a security.
   */
  private final class FixingRetriever extends FinancialSecurityVisitorAdapter<Result<HistoricalTimeSeriesBundle>> {

    private final Environment _env;

    /**
     * end date of the fixing series required
     */
    private final LocalDate _valuationDate;

    /**
     * @param env the environment used for the calculations
     * @param valuationDate the valuation date
     */
    private FixingRetriever(Environment env, LocalDate valuationDate) {
      _env = env;
      _valuationDate = valuationDate;
    }

    /**
     * Returns a time series bundle of the previous month's market values for the specified security.
     *
     * @param dataField the data field, usually Market_Value
     * @param period the period of time to return data for
     * @param ids the externalIdBundles to get series for
     * @return a historical time series bundle
     */
    private Result<HistoricalTimeSeriesBundle> getTimeSeriesBundle(String dataField,
                                                                   LocalDate start,
                                                                   Period period,
                                                                   ExternalIdBundle... ids) {
      HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
      Result<?> result = Result.success(true);
      for (ExternalIdBundle id : ids) {
        Result<HistoricalTimeSeries> series = getPreviousPeriodValues(id, period, start);
        if (series.isSuccess()) {
          bundle.add(dataField, id, series.getValue());
        } else {
          result = Result.failure(result, series);
        }
      }
      if (result.isSuccess()) {
        return Result.success(bundle);
      }
      return Result.failure(result);
    }

    /**
     * Returns a time series bundle of the previous month's market values for the specified security.
     *
     * @param dataField the data field, usually Market_Value
     * @param period the period of time to return data for
     * @param ids the externalIdBundles to get series for
     * @return a historical time series bundle
     */
    private Result<HistoricalTimeSeriesBundle> getTimeSeriesBundle(String dataField,
                                                                   Period period,
                                                                   ExternalIdBundle... ids) {
      return getTimeSeriesBundle(dataField, _valuationDate, period, ids);
    }

    /**
     * Returns a time series of the previous periods values for the specified external id
     *
     * @param id the external id of used to lookup the field values.
     * @param length the length of time to get values for.
     * @param date the date to get values for.
     * @return the time series result
     */
    private Result<HistoricalTimeSeries> getPreviousPeriodValues(ExternalIdBundle id,
                                                                 Period length,
                                                                 LocalDate date) {
      LocalDate periodStartDate = (date.isBefore(_valuationDate) ? date : _valuationDate).minus(length);
      LocalDate periodEndDate = date.isAfter(_valuationDate) ? date : _valuationDate;
      return getHistoricalTimeSeriesResult(_env, id, periodStartDate, periodEndDate);
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE,
                                 ONE_MONTH,
                                 security.getExternalIdBundle(),
                                 security.getUnderlyingId().toBundle());
    }
    
    @Override
    public Result<HistoricalTimeSeriesBundle> visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, ONE_MONTH, security.getExternalIdBundle());
    }
    
    @Override
    public Result<HistoricalTimeSeriesBundle> visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, ONE_MONTH, security.getExternalIdBundle());
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitSwaptionSecurity(SwaptionSecurity security) {
      if (security.getCurrency().equals(Currency.BRL)) {
        throw new UnsupportedOperationException("Fixing series for Brazilian swaptions not yet implemented");
      }
      return Result.success(EMPTY_TIME_SERIES_BUNDLE);
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitBondFutureSecurity(BondFutureSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, ONE_MONTH, security.getExternalIdBundle());
    }
    
    @Override
    public Result<HistoricalTimeSeriesBundle> visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, ONE_MONTH, security.getExternalIdBundle());
    }   
    
    @Override
    public Result<HistoricalTimeSeriesBundle> visitBondFutureOptionSecurity(BondFutureOptionSecurity security) {
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, ONE_MONTH, security.getExternalIdBundle());
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitInterestRateSwapSecurity(InterestRateSwapSecurity security) {
      Collection<ExternalIdBundle> ids = new ArrayList<>();
      for (FloatingInterestRateSwapLeg leg : security.getLegs(FloatingInterestRateSwapLeg.class)) {
        ExternalId id = leg.getFloatingReferenceRateId();
        ids.add(id.toBundle());
      }
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE,
                                 security.getEffectiveDate(),
                                 Period.ofYears(1),
                                 ids.toArray(new ExternalIdBundle[ids.size()]));
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security) {
      Collection<ExternalIdBundle> ids = new ArrayList<>();
      SwapLeg payLeg = security.getPayLeg();
      SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg instanceof InflationIndexSwapLeg) {
        ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
        ids.add(id.toBundle());
      }
      if (receiveLeg instanceof InflationIndexSwapLeg) {
        ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
        ids.add(id.toBundle());
      }

      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE,
                                 security.getEffectiveDate().toLocalDate(),
                                 Period.ofYears(1),
                                 ids.toArray(new ExternalIdBundle[ids.size()]));
    }

    @Override
    public Result<HistoricalTimeSeriesBundle> visitForwardRateAgreementSecurity(ForwardRateAgreementSecurity security) {
      ExternalIdBundle id = security.getUnderlyingId().toBundle();
      PeriodFrequency period = PeriodFrequency.convertToPeriodFrequency(security.getIndexFrequency());
      return getTimeSeriesBundle(MarketDataRequirementNames.MARKET_VALUE, period.getPeriod(), id);
    }

  }

  /**
   * Wraps the timeseries call and return a Result
   * @param id the id
   * @param startDate the start date
   * @param endDate the end date
   * @return the time series Result
   */
  private Result<HistoricalTimeSeries> getHistoricalTimeSeriesResult(Environment env,
                                                                     ExternalIdBundle id,
                                                                     LocalDate startDate,
                                                                     LocalDate endDate) {
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    Result<LocalDateDoubleTimeSeries> seriesResult = _historicalMarketDataFn.getMarketValues(env, id, dateRange);

    if (!seriesResult.isSuccess()) {
      return Result.failure(seriesResult);
    }
    LocalDateDoubleTimeSeries timeSeries = seriesResult.getValue();
    if (timeSeries.isEmpty()) {
      return Result.failure(FailureStatus.MISSING_DATA, "Time series for {} is empty", id);
    } else {
      HistoricalTimeSeries historicalTimeSeries = new LegacyHistoricalTimeSeries(timeSeries);
      return Result.success(historicalTimeSeries);
    }
  }

  private static class LegacyHistoricalTimeSeries implements HistoricalTimeSeries {

    private LocalDateDoubleTimeSeries _timeSeries;

    private LegacyHistoricalTimeSeries(LocalDateDoubleTimeSeries timeSeries) {
      _timeSeries = timeSeries;
    }

    @Override
    public UniqueId getUniqueId() {
      throw new UnsupportedOperationException("getUniqueId not supported");
    }

    @Override
    public LocalDateDoubleTimeSeries getTimeSeries() {
      return _timeSeries;
    }
  }
}
