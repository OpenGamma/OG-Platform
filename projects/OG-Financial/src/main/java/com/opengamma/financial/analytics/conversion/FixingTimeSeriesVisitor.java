/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 * Produces a value requirement that will query the fixing time series for a security.
 */
public class FixingTimeSeriesVisitor extends FinancialSecurityVisitorAdapter<ValueRequirement> { //CSIGNORE

  //TODO a lot of this code is repeated in FixedIncomeConverterDataProvider - that class should use this one

  private final ConventionBundleSource _conventionSource;
  private final HistoricalTimeSeriesResolver _resolver;
  private final DateConstraint _now;

  public FixingTimeSeriesVisitor(final ConventionBundleSource conventionSource, final HistoricalTimeSeriesResolver resolver, final DateConstraint now) {
    _conventionSource = conventionSource;
    _resolver = resolver;
    _now = now;
  }

  @Override
  public ValueRequirement visitSwapSecurity(final SwapSecurity security) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    if (type != InterestRateInstrumentType.SWAP_FIXED_IBOR &&
        type != InterestRateInstrumentType.SWAP_FIXED_OIS &&
        type != InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD) {
      throw new OpenGammaRuntimeException("Can only get series for fixed / float swaps; have " + type);
    }
    final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) (security.getPayLeg() instanceof FixedInterestRateLeg ? security.getReceiveLeg() : security.getPayLeg());
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    return getIndexTimeSeries(floatingLeg, swapStartDate, _now, true, _resolver);
  }

  private ValueRequirement getIndexTimeSeries(final FloatingInterestRateLeg leg, final ZonedDateTime swapEffectiveDate, final DateConstraint now,
      final boolean includeEndDate, final HistoricalTimeSeriesResolver resolver) {
    final FloatingInterestRateLeg floatingLeg = leg;
    final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
    final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(30); // To catch first fixing. SwapSecurity does not have this date.
    final HistoricalTimeSeriesResolutionResult ts = resolver.resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(ts, MarketDataRequirementNames.MARKET_VALUE, DateConstraint.of(startDate), true, now, includeEndDate);
  }

  public static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final HistoricalTimeSeries ts) {
    final LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
    // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    final ZonedDateTime[] instants = new ZonedDateTime[localDateTS.size()];
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext(); ) {
      final LocalDate date = it.nextTime();
      instants[it.currentIndex()] = date.atStartOfDay(ZoneOffset.UTC);
    }
    return ImmutableZonedDateTimeDoubleTimeSeries.of(instants, localDateTS.valuesArrayFast(), ZoneOffset.UTC);
  }

  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateLeg floatingLeg) {
    if (floatingLeg.getFloatingRateType().isIbor() || floatingLeg.getFloatingRateType().equals(FloatingRateType.OIS) || floatingLeg.getFloatingRateType().equals(FloatingRateType.CMS)) {
      return getIndexIdBundle(floatingLeg.getFloatingReferenceRateId());
    }
    return ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
  }

  private ExternalIdBundle getIndexIdBundle(final ExternalId indexId) {
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
    }
    return indexConvention.getIdentifiers();
  }
}
