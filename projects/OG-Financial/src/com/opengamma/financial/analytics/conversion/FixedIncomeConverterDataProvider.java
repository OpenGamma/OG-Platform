/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONSimplifiedDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
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
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * Convert an OG-Financial Security to its OG-Analytics Derivative form as seen from now
 */
public class FixedIncomeConverterDataProvider {

  private final ConventionBundleSource _conventionSource;
  private final HistoricalTimeSeriesResolver _timeSeriesResolver;

  public FixedIncomeConverterDataProvider(final ConventionBundleSource conventionSource, final HistoricalTimeSeriesResolver timeSeriesResolver) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
    _conventionSource = conventionSource;
    _timeSeriesResolver = timeSeriesResolver;
  }

  /**
   * Implementation of the conversion for a given instrument.
   */
  protected abstract class Converter<S extends Security, D extends InstrumentDefinition<?>> {

    /**
     * Returns the time series requirements that will be needed for the {@link #convert} method.
     * 
     * @param security the security, not null
     * @param curveNames the names of the curves, not null
     * @return the set of requirements, the empty set if nothing is required, null if the conversion will not be possible (for example a missing timeseries)
     */
    public abstract Set<ValueRequirement> getTimeSeriesRequirements(S security, String[] curveNames);

    /**
     * Converts the "security" and "definition" form to its "derivative" form.
     * 
     * @param security the security, not null
     * @param definition the definition, not null
     * @param now the observation time, not null
     * @param curveNames the names of the curves, not null
     * @param timeSeries the bundle containing timeseries produced to satisfy those returned by {@link #getTimeSeriesRequirements}
     * @return the derivative form, not null
     */
    public abstract InstrumentDerivative convert(S security, D definition, ZonedDateTime now, String[] curveNames, HistoricalTimeSeriesBundle timeSeries);

  }

  @SuppressWarnings("rawtypes")
  protected Converter getConverter(final Security security, final InstrumentDefinition<?> definition) {
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition to convert was null for security " + security);
    }
    if (security instanceof BondFutureSecurity) {
      return _bondFutureSecurity;
    }
    if (security instanceof FRASecurity) {
      return _fraSecurity;
    }
    if (security instanceof CapFloorSecurity) {
      if (((CapFloorSecurity) security).isIbor()) {
        return _capFloorIborSecurity;
      }
      return _capFloorCMSSecurity;
    }
    if (security instanceof InterestRateFutureSecurity) {
      return _irFutureSecurity;
    }
    if (security instanceof IRFutureOptionSecurity) {
      if (definition instanceof InterestRateFutureOptionMarginTransactionDefinition) {
        return _irFutureOptionSecurity;
      }
    }
    if (security instanceof SwapSecurity) {
      if (definition instanceof SwapFixedONSimplifiedDefinition) {
        return _default;
      }
      return _swapSecurity;
    }
    if (security instanceof CapFloorCMSSpreadSecurity) {
      return _capFloorCMSSpreadSecurity;
    }
    return _default;
  }

  @SuppressWarnings("unchecked")
  public Set<ValueRequirement> getConversionTimeSeriesRequirements(final Security security, final InstrumentDefinition<?> definition, final String[] curveNames) {
    return getConverter(security, definition).getTimeSeriesRequirements(security, curveNames);
  }

  @SuppressWarnings("unchecked")
  public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesBundle timeSeries) {
    return getConverter(security, definition).convert(security, definition, now, curveNames, timeSeries);
  }

  protected ConventionBundleSource getConventionSource() {
    return _conventionSource;
  }

  protected HistoricalTimeSeriesResolver getTimeSeriesResolver() {
    return _timeSeriesResolver;
  }

  private final Converter<BondFutureSecurity, BondFutureDefinition> _bondFutureSecurity = new Converter<BondFutureSecurity, BondFutureDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final BondFutureSecurity security, final String[] curveNames) {
      return Collections.emptySet();
    }

    @Override
    public InstrumentDerivative convert(final BondFutureSecurity security, final BondFutureDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      // TODO - CASE - Future refactor - See notes in convert(InterestRateFutureSecurity)
      // Get the time-dependent reference data required to price the Analytics Derivative
      final Double referencePrice = 0.0;
      // Construct the derivative as seen from now
      return definition.toDerivative(now, referencePrice, curveNames);
    }

  };

  private final Converter<FRASecurity, ForwardRateAgreementDefinition> _fraSecurity = new Converter<FRASecurity, ForwardRateAgreementDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final FRASecurity security, final String[] curveNames) {
      final ExternalId indexId = security.getUnderlyingId();
      final ConventionBundle indexConvention = getConventionSource().getConventionBundle(indexId);
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
      }
      final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(indexIdBundle, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @Override
    public InstrumentDerivative convert(final FRASecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId indexId = security.getUnderlyingId();
      final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
      }
      final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
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

  };

  private final Converter<CapFloorSecurity, AnnuityCapFloorIborDefinition> _capFloorIborSecurity = new Converter<CapFloorSecurity, AnnuityCapFloorIborDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorSecurity security, final String[] curveNames) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getUnderlyingId().toBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorIborDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId());
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

  };

  private final Converter<CapFloorSecurity, AnnuityCapFloorCMSDefinition> _capFloorCMSSecurity = new Converter<CapFloorSecurity, AnnuityCapFloorCMSDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorSecurity security, final String[] curveNames) {
      final ExternalId id = security.getUnderlyingId();
      final ZonedDateTime capStartDate = security.getStartDate();
      final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
      final ValueRequirement requirement = getIndexTimeSeriesRequirement(getIndexIdBundle(id), startDate);
      if (requirement == null) {
        return null;
      }
      return Collections.singleton(requirement);
    }

    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorCMSDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final DoubleTimeSeries<ZonedDateTime> indexTS = getIndexTimeSeries(getIndexIdBundle(id), now.getZone(), timeSeries);
      return definition.toDerivative(now, indexTS, curveNames);
    }

  };

  private final Converter<InterestRateFutureSecurity, InterestRateFutureDefinition> _irFutureSecurity = new Converter<InterestRateFutureSecurity, InterestRateFutureDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final InterestRateFutureSecurity security, final String[] curveNames) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, true));
    }

    @Override
    public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + security);
      }
      final int length = ts.getTimeSeries().size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
      return definition.toDerivative(now, lastMarginPrice, curveNames);
    }

  };

  private final Converter<IRFutureOptionSecurity, InterestRateFutureOptionMarginTransactionDefinition> _irFutureOptionSecurity = new Converter<IRFutureOptionSecurity, InterestRateFutureOptionMarginTransactionDefinition>() { // CSIGNORE

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final IRFutureOptionSecurity security, final String[] curveNames) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @Override
    public InstrumentDerivative convert(final IRFutureOptionSecurity security, final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime now,
        final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
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

  };

  private final Converter<SwapSecurity, SwapDefinition> _swapSecurity = new Converter<SwapSecurity, SwapDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final SwapSecurity security, final String[] curveNames) {
      Validate.notNull(security, "security");
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate();
      final ZonedDateTime swapStartLocalDate = ZonedDateTime.of(swapStartDate.toLocalDate(), LocalTime.of(0, 0), TimeZone.UTC);
      final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartLocalDate);
      final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartLocalDate);
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      if (payLegTS != null) {
        requirements.add(payLegTS);
      }
      if (receiveLegTS != null) {
        requirements.add(receiveLegTS);
      }
      return requirements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InstrumentDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate();
      final ZonedDateTime swapStartLocalDate = ZonedDateTime.of(swapStartDate.toLocalDate(), LocalTime.of(0, 0), TimeZone.UTC);
      final boolean includeCurrentDatesFixing = true;
      final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartLocalDate, now,
          includeCurrentDatesFixing,
          timeSeries);
      final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartLocalDate, now,
          includeCurrentDatesFixing, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
          return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        }
        try {
          return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (receiveLegTS != null) {
        if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
          try {
            return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }

  };

  private final Converter<CapFloorCMSSpreadSecurity, AnnuityCapFloorCMSSpreadDefinition> _capFloorCMSSpreadSecurity = new Converter<CapFloorCMSSpreadSecurity, AnnuityCapFloorCMSSpreadDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorCMSSpreadSecurity security, final String[] curveNames) {
      final ExternalId longId = security.getLongId();
      final ExternalId shortId = security.getShortId();
      final ZonedDateTime capStartDate = security.getStartDate();
      final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
      final ValueRequirement indexLongTS = getIndexTimeSeriesRequirement(getIndexIdBundle(longId), startDate);
      if (indexLongTS == null) {
        return null;
      }
      final ValueRequirement indexShortTS = getIndexTimeSeriesRequirement(getIndexIdBundle(shortId), startDate);
      if (indexShortTS == null) {
        return null;
      }
      return ImmutableSet.of(indexLongTS, indexShortTS);
    }

    @Override
    public InstrumentDerivative convert(final CapFloorCMSSpreadSecurity security, final AnnuityCapFloorCMSSpreadDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId longId = security.getLongId();
      final ExternalId shortId = security.getShortId();
      final DoubleTimeSeries<ZonedDateTime> indexLongTS = getIndexTimeSeries(getIndexIdBundle(longId), now.getZone(), timeSeries);
      final DoubleTimeSeries<ZonedDateTime> indexShortTS = getIndexTimeSeries(getIndexIdBundle(shortId), now.getZone(), timeSeries);
      final DoubleTimeSeries<ZonedDateTime> indexSpreadTS = indexLongTS.subtract(indexShortTS);
      return definition.toDerivative(now, indexSpreadTS, curveNames);
    }

  };

  private final Converter<Security, InstrumentDefinition<?>> _default = new Converter<Security, InstrumentDefinition<?>>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final Security security, final String[] curveNames) {
      return Collections.emptySet();
    }

    @Override
    public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      return definition.toDerivative(now, curveNames);
    }

  };

  private ValueRequirement getIndexTimeSeriesRequirement(final InterestRateInstrumentType type, final SwapLeg leg, final ZonedDateTime swapEffectiveDate) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(360);
      // Implementation note: To catch first fixing. SwapSecurity does not have this date.
      final HistoricalTimeSeriesResolutionResult ts = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (ts == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(ts, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
    }
    return null;
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final InterestRateInstrumentType type, final SwapLeg leg, final ZonedDateTime swapEffectiveDate, final ZonedDateTime now,
      final boolean includeEndDate, final HistoricalTimeSeriesBundle timeSeries) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      // Implementation note: To catch first fixing. SwapSecurity does not have this date.
      if (now.isBefore(swapEffectiveDate)) { // TODO: review if this is the correct condition
        return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
      }
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (ts == null) {
        //throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
        final ZonedDateTime[] times = new ZonedDateTime[600];
        final double[] values = new double[600];
        ZonedDateTime temp = DateUtils.getUTCDate(2012, 7, 21);
        for (int i = 599; i >= 0; i--) {
          times[i] = temp;
          values[i] = 0.005;
          temp = temp.minusDays(1);
        }
        return new ArrayZonedDateTimeDoubleTimeSeries(times, values);
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
   * 
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexIdBundle(final ExternalId indexId) {
    final ConventionBundle indexConvention = getConventionSource().getConventionBundle(indexId);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
    }
    return indexConvention.getIdentifiers();
  }

  private ValueRequirement getIndexTimeSeriesRequirement(final ExternalIdBundle id, final LocalDate startDate) {
    final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
  }

  /**
   * Returns the time series to be used in the toDerivative method.
   * 
   * @param id The ExternalId bundle.
   * @param startDate The time series start date (included in the time series).
   * @param timeZone The time zone to use for the returned series
   * @param dataSource The time series data source.
   * @return The time series.
   */
  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final ExternalIdBundle id, final TimeZone timeZone, final HistoricalTimeSeriesBundle timeSeries) {
    final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
    // Implementation note: the normalization take place in the getHistoricalTimeSeries
    if (ts == null) {
      //throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString());
      final ZonedDateTime[] times = new ZonedDateTime[600];
      final double[] values = new double[600];
      ZonedDateTime now = DateUtils.getUTCDate(2012, 7, 21);
      for (int i = 599; i >= 0; i--) {
        times[i] = now;
        values[i] = 0.005;
        now = now.minusDays(1);
      }
      return new ArrayZonedDateTimeDoubleTimeSeries(times, values);
    }
    if (ts.getTimeSeries().isEmpty()) {
      return ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
    }
    final FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    final FastLongDoubleTimeSeries convertedTS = localDateTS.toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(0, 0); // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(timeZone, fixingTime), convertedTS);
  }

}
