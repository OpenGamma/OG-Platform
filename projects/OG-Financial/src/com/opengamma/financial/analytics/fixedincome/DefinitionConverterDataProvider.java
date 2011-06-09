/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.security.Security;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DefinitionConverterDataProvider {
  private final String _dataSourceName;
  private final String _fieldName;
  private final String _dataProvider = "CMPL"; // TODO: totally fix this.

  public DefinitionConverterDataProvider(String dataSourceName, String fieldName) {
    _dataSourceName = dataSourceName;
    _fieldName = fieldName;
  }

  public InterestRateDerivative convert(Security security, FixedIncomeInstrumentConverter<?> definition,
      ZonedDateTime now, String[] curveNames, HistoricalDataSource dataSource) {
    if (security instanceof SwapSecurity) {
      return convert((SwapSecurity) security, (SwapDefinition) definition, now, curveNames, dataSource);
    }
    return definition.toDerivative(now, curveNames);
  }

  @SuppressWarnings("unchecked")
  public InterestRateDerivative convert(SwapSecurity security, SwapDefinition definition, ZonedDateTime now,
      String[] curveNames, HistoricalDataSource dataSource) {
    Validate.notNull(security, "security");
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartDate, now, dataSource);
    final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartDate, now, dataSource);
    if (payLegTS != null) {
      if (receiveLegTS != null) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, receiveLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS}, curveNames);
    }
    if (receiveLegTS != null) {
      return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS}, curveNames);
    }
    throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(InterestRateInstrumentType type, final SwapLeg leg,
      final ZonedDateTime swapStartDate, ZonedDateTime now, HistoricalDataSource dataSource) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final Identifier indexID = floatingLeg.getFloatingReferenceRateIdentifier();
      final IdentifierBundle id = indexID.toBundle(); 
      LocalDate startDate = swapStartDate.isBefore(now) ? swapStartDate.toLocalDate().minusDays(7) : now.toLocalDate()
          .minusDays(7);
      final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = dataSource
          .getHistoricalData(id,
                _dataSourceName, _dataProvider, _fieldName, startDate, true,
              now.toLocalDate(), true);
      if (tsPair.getKey() == null) {
        throw new OpenGammaRuntimeException("Could not get time series of underlying index " + indexID.toString());
      }
      FastBackedDoubleTimeSeries<LocalDate> localDateTS = tsPair.getSecond();
      //TODO this normalization should not be done here
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR) {
        localDateTS = localDateTS.divide(100);
      } else if (type == InterestRateInstrumentType.SWAP_IBOR_IBOR) { //TODO not really - valid for tenor swaps but we really need to normalize the time series rather than doing it here
        localDateTS = localDateTS.divide(10000);
      } else {
        throw new OpenGammaRuntimeException("Couldn't identify swap type");
      }
      FastLongDoubleTimeSeries convertedTS = localDateTS
          .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
      LocalTime fixingTime = LocalTime.of(11, 0);
      return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
          convertedTS);
    }
    return null;
  }
}
