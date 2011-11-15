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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
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

  private final String _fieldName = HistoricalTimeSeriesFields.LAST_PRICE;
  private final ConventionBundleSource _conventionSource;

  public FixedIncomeConverterDataProvider(final ConventionBundleSource conventionSource) {
    _conventionSource = conventionSource;
  }

  public InterestRateDerivative convert(final Security security, final FixedIncomeInstrumentDefinition<?> definition,
      final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
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
      return convert((SwapSecurity) security, (SwapDefinition) definition, now, curveNames, dataSource);
    }
    if (security instanceof CapFloorCMSSpreadSecurity) {
      return convert((CapFloorCMSSpreadSecurity) security, (AnnuityCapFloorCMSSpreadDefinition) definition, now, curveNames, dataSource);
    }
    return definition.toDerivative(now, curveNames);
  }

  public InterestRateDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {

    // Get the time-dependent reference data required to price the Analytics Derivative
    Double referencePrice = 0.0;

    // TODO - CASE - Future refactor - cleanup the following InterestRateFutureTransactionDefinition rubbish
    /* Here are some tools available:
    final ExternalIdBundle id = security.getExternalIdBundle();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7)); // FIXME Hardcoded behaviour
    final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(_fieldName, id, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) { throw new OpenGammaRuntimeException("Could not get price time series for " + security); }
    final Double referencePrice = ts.getTimeSeries().getLatestValue() / 100;    
    */

    // Construct the derivative as seen from now
    return definition.toDerivative(now, referencePrice, curveNames);

  }

  public InterestRateDerivative convert(final BondFutureSecurity security, final BondFutureDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {

    // TODO - CASE - Future refactor - See notes in convert(InterestRateFutureSecurity)
    // Get the time-dependent reference data required to price the Analytics Derivative
    Double referencePrice = 0.0;

    // Construct the derivative as seen from now
    return definition.toDerivative(now, referencePrice, curveNames);

  }

  public InterestRateDerivative convert(final IRFutureOptionSecurity security, final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime now,
        final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final ExternalIdBundle id = ExternalIdBundle.of(security.getUnderlyingId());
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource
            .getHistoricalTimeSeries(_fieldName, id, null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + security);
    }
    final int length = ts.getTimeSeries().size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getUnderlyingId() + " was empty");
    }
    final double lastMarginPrice = ts.getTimeSeries().getValueAt(length - 1);
    return definition.toDerivative(now, lastMarginPrice, curveNames);
  }

  public InterestRateDerivative convert(final FRASecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final ExternalId id = security.getUnderlyingId();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(_fieldName, ExternalIdBundle.of(id), null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + id);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    //TODO this normalization should not be done here
    localDateTS = localDateTS.divide(100);
    final FastLongDoubleTimeSeries convertedTS = localDateTS
        .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final DoubleTimeSeries<ZonedDateTime> indexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
        convertedTS);
    return definition.toDerivative(now, indexTS, curveNames);
  }

  public InterestRateDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorIborDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final ExternalId id = security.getUnderlyingId();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(_fieldName, ExternalIdBundle.of(id), null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + id);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    //TODO this normalization should not be done here
    localDateTS = localDateTS.divide(100);
    final FastLongDoubleTimeSeries convertedTS = localDateTS
        .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final DoubleTimeSeries<ZonedDateTime> indexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
        convertedTS);
    return definition.toDerivative(now, indexTS, curveNames);
  }
  
  public InterestRateDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorCMSDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final ExternalId id = security.getUnderlyingId();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(_fieldName, ExternalIdBundle.of(id), null, null, startDate, true, now.toLocalDate(), false);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + id);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
    //TODO this normalization should not be done here
    localDateTS = localDateTS.divide(100);
    final FastLongDoubleTimeSeries convertedTS = localDateTS
        .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final DoubleTimeSeries<ZonedDateTime> indexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
        convertedTS);
    return definition.toDerivative(now, indexTS, curveNames);
  }
  
  public InterestRateDerivative convert(final CapFloorCMSSpreadSecurity security, final AnnuityCapFloorCMSSpreadDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final ExternalId longId = security.getLongId();
    final LocalDate startDate = DateUtils.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries longTS = dataSource
          .getHistoricalTimeSeries(_fieldName, ExternalIdBundle.of(longId), null, null, startDate, true, now.toLocalDate(), false);
    if (longTS == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + longId);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateLongTS = longTS.getTimeSeries();
    final ExternalId shortId = security.getShortId();
    final HistoricalTimeSeries shortTS = dataSource
          .getHistoricalTimeSeries(_fieldName, ExternalIdBundle.of(longId), null, null, startDate, true, now.toLocalDate(), false);
    if (shortTS == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + shortId);
    }
    FastBackedDoubleTimeSeries<LocalDate> localDateShortTS = shortTS.getTimeSeries();   
    //TODO this normalization should not be done here
    FastBackedDoubleTimeSeries<LocalDate> localDateSpreadTS = localDateLongTS.subtract(localDateShortTS).divide(100);
    final FastLongDoubleTimeSeries convertedSpreadTS = localDateSpreadTS
        .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final DoubleTimeSeries<ZonedDateTime> spreadIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
        convertedSpreadTS);
    return definition.toDerivative(now, spreadIndexTS, curveNames);
  }
  
  @SuppressWarnings("unchecked")
  public InterestRateDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    Validate.notNull(security, "security");
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartDate, now, dataSource);
    final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartDate, now, dataSource);
    if (payLegTS != null) {
      if (payLegTS.isEmpty()) {
        throw new OpenGammaRuntimeException("Time series was empty for floating leg for swap: reference index is " + ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
      }
      if (receiveLegTS != null) {
        if (receiveLegTS.isEmpty()) {
          throw new OpenGammaRuntimeException("Time series was empty for floating leg for swap: reference index is " + ((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
        }
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, payLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS}, curveNames);      
    }
    if (receiveLegTS != null) {
      if (receiveLegTS.isEmpty()) {
        throw new OpenGammaRuntimeException("Time series was empty for floating leg for swap: reference index is " + ((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS, receiveLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS}, curveNames);
    }
    throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final InterestRateInstrumentType type, final SwapLeg leg,
      final ZonedDateTime swapStartDate, final ZonedDateTime now, final HistoricalTimeSeriesSource dataSource) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      final LocalDate startDate = swapStartDate.isBefore(now) ? swapStartDate.toLocalDate().minusDays(7) : now.toLocalDate()
          .minusDays(7);
      final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(_fieldName, id, null, null, startDate, true, now.toLocalDate(), false);
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
      }
      if (ts.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Empty time series for underlying index " + id.getExternalIds().toString() + 
            " between dates " + startDate + " and " + now.toLocalDate() + ", bundle used was " + id);        
      }
      FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_CMS || 
          type == InterestRateInstrumentType.SWAP_IBOR_CMS || type == InterestRateInstrumentType.SWAP_IBOR_CMS ||
          type == InterestRateInstrumentType.SWAP_CMS_CMS) {
        localDateTS = localDateTS.divide(100);
      } else if (type == InterestRateInstrumentType.SWAP_IBOR_IBOR) { //TODO not really - valid for tenor swaps but we really need to normalize the time series rather than doing it here
        localDateTS = localDateTS.divide(10000);
      } else {
        throw new OpenGammaRuntimeException("Couldn't identify swap type");
      }
      final FastLongDoubleTimeSeries convertedTS = localDateTS
          .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
      final LocalTime fixingTime = LocalTime.of(11, 0);
      return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
          convertedTS);
    }
    return null;
  }
  
  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateLeg floatingLeg) {
    if (floatingLeg.getFloatingRateType().isIbor()) {
      final ExternalId indexId = floatingLeg.getFloatingReferenceRateId();
      ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
      if (indexConvention == null) {
        //TODO remove this immediately
        indexConvention = _conventionSource.getConventionBundle(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, indexId.getValue()));
      }
      return indexConvention.getIdentifiers();
    } 
    return ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
  }
}
