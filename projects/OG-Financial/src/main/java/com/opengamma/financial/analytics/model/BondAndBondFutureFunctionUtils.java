/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_TIME_SERIES;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeWithEntityConverter;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods that are used in functions that calculate bond analytics.
 */
public class BondAndBondFutureFunctionUtils {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BondAndBondFutureFunctionUtils.class);

  /**
   * Gets any additional value requirements that are required for conversion from securities
   * to analytics objects. For bond futures, the future price time series is required. For all
   * other securities, an empty set is returned
   * @param security The security, not null
   * @param timeSeriesResolver The time series resolver, not null if the security is a bond future
   * @return The set of requirements
   */
  public static Set<ValueRequirement> getConversionRequirements(final FinancialSecurity security, final HistoricalTimeSeriesResolver timeSeriesResolver) {
    ArgumentChecker.notNull(security, "security");
    if (security instanceof BondFutureSecurity) {
      ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
      final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
      final HistoricalTimeSeriesResolutionResult timeSeries = timeSeriesResolver.resolve(externalIdBundle, null, null, null,
          MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        s_logger.error("Could not resolve time series for {}", externalIdBundle);
        return Collections.emptySet();
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, true));
    }
    if (security instanceof InflationBondSecurity) {
      ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
      final ExternalIdBundle externalIdBundle = ExternalIdBundle.of(ExternalId.parse(((InflationBondSecurity) security).attributes().get().get("ReferenceIndexId")));
      final HistoricalTimeSeriesResolutionResult timeSeries = timeSeriesResolver.resolve(externalIdBundle, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        s_logger.error("Could not resolve time series for {}", externalIdBundle);
        return Collections.emptySet();
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(12)), true, DateConstraint.VALUATION_TIME, true));
    }
    return Collections.emptySet();
  }

  /**
   * Converts a bond or bond future trade into the {@link InstrumentDefinition} form that is used in
   * the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @return The definition form of a bond or bond future security
   */
  public static InstrumentDefinition<?> getDefinition(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(target.getType() == ComputationTargetType.TRADE, "Computation target must be a trade");
    final Trade trade = target.getTrade();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    final LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(context);
    final BondAndBondFutureTradeWithEntityConverter converter = new BondAndBondFutureTradeWithEntityConverter(holidaySource, conventionBundleSource,
        conventionSource, regionSource, securitySource, legalEntitySource);
    return converter.convert(trade);
  }

  private static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
    // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext();) {
      final LocalDate date = it.nextTime();
      final ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }

  /**
   * Converts a bond or bond future trade into the {@link InstrumentDerivative} form that is used in
   * pricing functions in the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @param inputs The function inputs, not null if the security is a {@link BondFutureSecurity}
   * @return The derivative form of the security
   */
  public static InstrumentDerivative getBondOrBondFutureDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date,
      final FunctionInputs inputs) {
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.isTrue(target.getType() == ComputationTargetType.TRADE, "Computation target must be a trade");
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    if (security instanceof BondSecurity & !(security instanceof InflationBondSecurity)) {
      return getBondDerivative(context, target, date);
    }
    if (security instanceof InflationBondSecurity) {
      final HistoricalTimeSeries indexPriceSeries = (HistoricalTimeSeries) inputs.getValue(HISTORICAL_TIME_SERIES);
      final LocalDateDoubleTimeSeries ts = indexPriceSeries.getTimeSeries();
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(date.getZone(), ts.multiply(100.0));
      return getBondInflationDerivative(context, target, date, indexTS);
    }
    if (security instanceof BillSecurity) {
      return getBillDerivative(context, target, date);
    }
    if (security instanceof BondFutureSecurity) {
      ArgumentChecker.notNull(inputs, "inputs");
      final HistoricalTimeSeries futurePriceSeries = (HistoricalTimeSeries) inputs.getValue(HISTORICAL_TIME_SERIES);
      return getBondFutureDerivative(context, target, date, futurePriceSeries);
    }
    throw new OpenGammaRuntimeException("Unsupported security type " + security.getClass());
  }

  /**
   * Converts a bond trade into the {@link InstrumentDerivative} form that is used in pricing
   * functions in the the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @return The derivative form of a bond security
   */
  private static InstrumentDerivative getBondDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date) {
    final InstrumentDefinition<?> definition = getDefinition(context, target, date);
    return definition.toDerivative(date);
  }

  /**
   * Converts a bond trade into the {@link InstrumentDerivative} form that is used in pricing
   * functions in the the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @return The derivative form of a bond security
   */
  private static InstrumentDerivative getBondInflationDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date,
      final ZonedDateTimeDoubleTimeSeries ts) {
    final InstrumentDefinition<?> definition = getDefinition(context, target, date);
    return ((BondCapitalIndexedTransactionDefinition<?>) definition).toDerivative(date, ts);
  }

  /**
   * Converts a bill trade into the {@link InstrumentDerivative} form that is used in pricing
   * functions in the the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @return The derivative form of a bill security
   */
  private static InstrumentDerivative getBillDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date) {
    final InstrumentDefinition<?> definition = getDefinition(context, target, date);
    return definition.toDerivative(date);
  }

  /**
   * Converts a bond future trade into the {@link InstrumentDerivative} form that is used in pricing
   * functions in the the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @param futurePriceSeries The bond future price time series, not null
   * @return The derivative form of a bond security
   * @throws OpenGammaRuntimeException If the bond future price series is empty
   */
  private static InstrumentDerivative getBondFutureDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date,
      final HistoricalTimeSeries futurePriceSeries) {
    final BondFutureSecurity security = (BondFutureSecurity) target.getTrade().getSecurity();
    ArgumentChecker.notNull(futurePriceSeries, "futurePriceSeries");
    final InstrumentDefinition<?> definition = getDefinition(context, target, date);
    final LocalDateDoubleTimeSeries ts = futurePriceSeries.getTimeSeries();
    final int length = ts.size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
    }
    final double lastMarginPrice = ts.getLatestValue();
    return ((BondFuturesTransactionDefinition) definition).toDerivative(date, lastMarginPrice);
  }
}
