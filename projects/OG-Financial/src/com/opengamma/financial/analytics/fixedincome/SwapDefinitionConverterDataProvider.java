/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SwapDefinitionConverterDataProvider implements
    SwapSecurityVisitor<Map<SwapLeg, DoubleTimeSeries<ZonedDateTime>>> {
  private final HistoricalTimeSeriesSource _dataSource;
  private final String _dataSourceName;
  private final String _fieldName;
  private final LocalDate _now;
  private final TimeZone _timeZone;

  public SwapDefinitionConverterDataProvider(final HistoricalTimeSeriesSource dataSource,
      final String dataSourceName, final String fieldName, final ZonedDateTime now) {
    Validate.notNull(dataSource, "data source");
    Validate.notNull(dataSourceName, "data source name");
    Validate.notNull(fieldName, "field name");
    Validate.notNull(now, "now");
    _dataSource = dataSource;
    _dataSourceName = dataSourceName;
    _fieldName = fieldName;
    _now = now.toLocalDate();
    _timeZone = now.getZone();
  }

  @Override
  public Map<SwapLeg, DoubleTimeSeries<ZonedDateTime>> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return visitSwapSecurity(security);
  }

  @Override
  public Map<SwapLeg, DoubleTimeSeries<ZonedDateTime>> visitSwapSecurity(final SwapSecurity security) {
    Validate.notNull(security, "security");
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(payLeg, swapStartDate);
    final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(receiveLeg, swapStartDate);
    final Map<SwapLeg, DoubleTimeSeries<ZonedDateTime>> result = new HashMap<SwapLeg, DoubleTimeSeries<ZonedDateTime>>();
    if (payLegTS != null) {
      result.put(payLeg, payLegTS);
    }
    if (receiveLegTS != null) {
      result.put(receiveLeg, receiveLegTS);
    }
    return result;
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final SwapLeg leg, final ZonedDateTime swapStartDate) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final UniqueIdentifier indexID = floatingLeg.getFloatingReferenceRateIdentifier();
      final IdentifierBundle id = null; //TODO //IdentifierBundle.of(indexID);
      final HistoricalTimeSeries hts = _dataSource.getHistoricalTimeSeries(id,
          _dataSourceName, null, _fieldName, swapStartDate.toLocalDate(), true, _now, false);
      if (hts == null) {
        throw new OpenGammaRuntimeException("Could not get time series of underlying index " + indexID.toString());
      }
      return hts.getTimeSeries().toZonedDateTimeDoubleTimeSeries(_timeZone);
    }
    return null;
  }
}
