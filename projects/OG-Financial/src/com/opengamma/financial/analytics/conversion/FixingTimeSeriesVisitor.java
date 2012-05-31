/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * 
 */
public class FixingTimeSeriesVisitor extends FinancialSecurityVisitorAdapter<DoubleTimeSeries<ZonedDateTime>[]> { //CSIGNORE
  //TODO a lot of this code is repeated in FixedIncomeConverterDataProvider - that class should use this one
  private final ConventionBundleSource _conventionSource;
  private final HistoricalTimeSeriesSource _dataSource;
  private final ZonedDateTime _now;

  public FixingTimeSeriesVisitor(final ConventionBundleSource conventionSource, final HistoricalTimeSeriesSource dataSource, final ZonedDateTime now) {
    _conventionSource = conventionSource;
    _dataSource = dataSource;
    _now = now;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleTimeSeries<ZonedDateTime>[] visitSwapSecurity(final SwapSecurity security) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    if (type != InterestRateInstrumentType.SWAP_FIXED_IBOR &&
        type != InterestRateInstrumentType.SWAP_FIXED_OIS &&
        type != InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD) {
      throw new OpenGammaRuntimeException("Can only get series for fixed / float swaps; have " + type);
    }
    final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) (security.getPayLeg() instanceof FixedInterestRateLeg ? security.getReceiveLeg() : security.getPayLeg());
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final DoubleTimeSeries<ZonedDateTime> fixingTS = getIndexTimeSeries(type, floatingLeg, swapStartDate, _now,
        true, _dataSource);
    return new DoubleTimeSeries[] {fixingTS };
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final InterestRateInstrumentType type, final FloatingInterestRateLeg leg, final ZonedDateTime swapEffectiveDate, final ZonedDateTime now,
      final boolean includeEndDate, final HistoricalTimeSeriesSource dataSource) {
    final FloatingInterestRateLeg floatingLeg = leg;
    final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
    final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(30); // To catch first fixing. SwapSecurity does not have this date.
    if (startDate.isAfter(now.toLocalDate()) || now.isBefore(swapEffectiveDate) || now.equals(swapEffectiveDate)) {
      return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
    }
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, null, startDate, true, now.toLocalDate(), includeEndDate);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
    }
    if (ts.getTimeSeries().isEmpty()) {
      return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
    }
    final FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    final FastLongDoubleTimeSeries convertedTS = localDateTS.toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(0, 0); // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime), convertedTS);
  }

  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateLeg floatingLeg) {
    if (floatingLeg.getFloatingRateType().isIbor() || floatingLeg.getFloatingRateType().equals(FloatingRateType.OIS) || floatingLeg.getFloatingRateType().equals(FloatingRateType.CMS)) {
      return getIndexIdBundle(floatingLeg.getFloatingReferenceRateId());
    } else {
      return ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
    }
  }

  private ExternalIdBundle getIndexIdBundle(final ExternalId indexId) {
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
    }
    return indexConvention.getIdentifiers();
  }
}
