/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedOISSimplifiedDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * Convert an OG-Financial Security to it's OG-Analytics Derivative form as seen from now
 */
public class FixedIncomeConverterDataProvider {
  private final ConventionBundleSource _conventionSource;

  public FixedIncomeConverterDataProvider(final ConventionBundleSource conventionSource) {
    _conventionSource = conventionSource;
  }

  public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition to convert was null for security " + security);
    }
    if (security instanceof BondFutureSecurity) {
      return convert((BondFutureSecurity) security, (BondFutureDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof FRASecurity) {
      return convert((FRASecurity) security, (ForwardRateAgreementDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof CapFloorSecurity) {
      if (((CapFloorSecurity) security).isIbor()) {
        return convert((CapFloorSecurity) security, (AnnuityCapFloorIborDefinition) definition, now, curveNames, dataSource);
      }
      return convert((CapFloorSecurity) security, (AnnuityCapFloorCMSDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof InterestRateFutureSecurity) {
      return convert((InterestRateFutureSecurity) security, (InterestRateFutureDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof IRFutureOptionSecurity) {
      if (definition instanceof InterestRateFutureOptionMarginTransactionDefinition) {
        return convert((IRFutureOptionSecurity) security, (InterestRateFutureOptionMarginTransactionDefinition) definition, now, curveNames, dataSource);
      }
    }
    if (security instanceof SwapSecurity) {
      if (definition instanceof SwapFixedOISSimplifiedDefinition) {
        return definition.toDerivative(now, curveNames);
      }
      return convert((SwapSecurity) security, (SwapDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof CapFloorCMSSpreadSecurity) {
      return convert((CapFloorCMSSpreadSecurity) security, (AnnuityCapFloorCMSSpreadDefinition) definition, now, curveNames, dataSource);
    }
    return definition.toDerivative(now, curveNames);

  }

  public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalIdBundle id = security.getExternalIdBundle();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusMonths(1));
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + security);
    }
    final int length = ts.getTimeSeries().size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getUnderlyingId() + " was empty");
    }
    final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
    return definition.toDerivative(now, lastMarginPrice, curveNames);
  }

  public InstrumentDerivative convert(final BondFutureSecurity security, final BondFutureDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {

    // TODO - CASE - Future refactor - See notes in convert(InterestRateFutureSecurity)
    // Get the time-dependent reference data required to price the Analytics Derivative
    final Double referencePrice = 0.0;

    // Construct the derivative as seen from now
    return definition.toDerivative(now, referencePrice, curveNames);

  }

  public InstrumentDerivative convert(final IRFutureOptionSecurity security, final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalIdBundle id = security.getExternalIdBundle();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusMonths(1));
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + security);
    }
    final int length = ts.getTimeSeries().size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getUnderlyingId() + " was empty");
    }
    final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
    return definition.toDerivative(now, lastMarginPrice, curveNames);
  }

  public InstrumentDerivative convert(final FRASecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalId indexId = security.getUnderlyingId();
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
    }
    final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    //TODO this normalization should not be done here
    localDateTS = localDateTS.divide(100);
    final FastLongDoubleTimeSeries convertedTS = localDateTS.toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(0, 0);
    final DoubleTimeSeries<ZonedDateTime> indexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime), convertedTS);
    // TODO: remove the zone
    return definition.toDerivative(now, indexTS, curveNames);
  }

  public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorIborDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalId id = security.getUnderlyingId();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, ExternalIdBundle.of(id), null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + id);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    //TODO this normalization should not be done here
    localDateTS = localDateTS.divide(100);
    final FastLongDoubleTimeSeries convertedTS = localDateTS.toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final DoubleTimeSeries<ZonedDateTime> indexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime), convertedTS);
    return definition.toDerivative(now, indexTS, curveNames);
  }

  /**
   * Convert from the "security" and "definition" version of a CMS cap/floor annuity to its "derivative" version.
   * @param security The security.
   * @param definition The definition version of the instrument.
   * @param now The conversion moment.
   * @param curveNames The name of the pricing curves.
   * @param dataSource The time series data source.
   * @return The "derivative" version of the instrument.
   */
  public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorCMSDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalId id = security.getUnderlyingId();
    final ZonedDateTime capStartDate = security.getStartDate();
    final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
    final DoubleTimeSeries<ZonedDateTime> indexTS = getIndexTimeSeries(getIndexIdBundle(id), startDate, now, true, dataSource);
    return definition.toDerivative(now, indexTS, curveNames);
  }

  /**
   * Convert from the "security" and "definition" version of a CMS spread cap/floor annuity to its "derivative" version.
   * @param security The security.
   * @param definition The definition version of the instrument.
   * @param now The conversion moment.
   * @param curveNames The name of the pricing curves.
   * @param dataSource The time series data source.
   * @return The "derivative" version of the instrument.
   */
  public InstrumentDerivative convert(final CapFloorCMSSpreadSecurity security, final AnnuityCapFloorCMSSpreadDefinition definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    final ExternalId longId = security.getLongId();
    final ExternalId shortId = security.getShortId();
    final ZonedDateTime capStartDate = security.getStartDate();
    final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
    final DoubleTimeSeries<ZonedDateTime> indexLongTS = getIndexTimeSeries(getIndexIdBundle(longId), startDate, now, true, dataSource);
    final DoubleTimeSeries<ZonedDateTime> indexShortTS = getIndexTimeSeries(getIndexIdBundle(shortId), startDate, now, true, dataSource);
    final DoubleTimeSeries<ZonedDateTime> indexSpreadTS = indexLongTS.subtract(indexShortTS);
    return definition.toDerivative(now, indexSpreadTS, curveNames);
  }

  @SuppressWarnings("unchecked")
  public InstrumentDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    Validate.notNull(security, "security");
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final boolean includeCurrentDatesFixing = true;
    final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartDate, now, includeCurrentDatesFixing,
        dataSource);
    final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartDate, now,
        includeCurrentDatesFixing, dataSource);
    if (payLegTS != null) {
      if (receiveLegTS != null) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, receiveLegTS}, curveNames);
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, payLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS}, curveNames);
    }
    if (receiveLegTS != null) {
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS, receiveLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS}, curveNames);
    }
    throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final InterestRateInstrumentType type, final SwapLeg leg, final ZonedDateTime swapEffectiveDate, final ZonedDateTime now,
      final boolean includeEndDate, final HistoricalTimeSeriesSource dataSource) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
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
    return null;
  }

  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateLeg floatingLeg) {
    if (floatingLeg.getFloatingRateType().isIbor() || floatingLeg.getFloatingRateType().equals(FloatingRateType.OIS) || floatingLeg.getFloatingRateType().equals(FloatingRateType.CMS)) {
      return getIndexIdBundle(floatingLeg.getFloatingReferenceRateId());
    } else {
      return ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
    }
  }

  /**
   * Returns the ExternalIDBundle associated to an ExternalId as stored in the convention source.
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexIdBundle(final ExternalId indexId) {
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
    }
    return indexConvention.getIdentifiers();
  }

  /**
   * Returns the time series to be used in the toDerivative method.
   * @param id The ExternalId bundle.
   * @param startDate The time series start date (included in the time series).
   * @param endDate The time series end date (included or not in the series according to includeEndDate flag).
   * @param includeEndDate Flag indicating if the end date should be included in the time series. The usual flag is "true".
   * @param dataSource The time series data source.
   * @return The time series.
   */
  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final ExternalIdBundle id, final LocalDate startDate, final ZonedDateTime endDate, final boolean includeEndDate,
      final HistoricalTimeSeriesSource dataSource) {
    final LocalDate endDateLocal = endDate.toLocalDate();
    if (startDate.isAfter(endDateLocal)) {
      return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
    }
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, null, startDate, true, endDateLocal, includeEndDate);
    // Implementation note: the normalization take place in the getHistoricalTimeSeries
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString());
    }
    if (ts.getTimeSeries().isEmpty()) {
      return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
    }
    final FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    final FastLongDoubleTimeSeries convertedTS = localDateTS.toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(0, 0); // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(endDate.getZone(), fixingTime), convertedTS);
  }

}
