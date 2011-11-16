/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class UnderlyingTypeToHistoricalTimeSeries {

  //private static final String IMPLIED_VOLATILITY = "OPT_IMPLIED_VOLATILITY_BST";
  //private static final String VOLUME = "VOLUME";

  
  public static LocalDateDoubleTimeSeries getSeries(final HistoricalTimeSeriesSource source, final String resolutionKey, final SecuritySource secMaster,
      final UnderlyingType underlying, final Security security) {
    return getSeries(source, resolutionKey, secMaster, underlying, security, null, null);
  }
  
  public static LocalDateDoubleTimeSeries getSeries(final HistoricalTimeSeriesSource source, final String resolutionKey, final SecuritySource secMaster,
      final UnderlyingType underlying, final Security security, final LocalDate startDate, final LocalDate endDate) {
    if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity option = (EquityOptionSecurity) security;
      switch (underlying) {
        case SPOT_PRICE:
          final Security underlyingSecurity = secMaster.getSecurity(ExternalIdBundle.of(option.getUnderlyingId()));
          final HistoricalTimeSeries hts = source.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, underlyingSecurity.getExternalIdBundle(), resolutionKey, startDate, true, endDate, true);
          if (hts == null) {
            throw new NullPointerException("Could not get time series pair for " + underlying + " for security " + security + "for " + resolutionKey + "/" + HistoricalTimeSeriesFields.LAST_PRICE);
          }
          return hts.getTimeSeries();
        default:
          throw new NotImplementedException("Don't know how to time series for " + underlying);
      }
    }
    throw new NotImplementedException("Can only get time series for options");
  }
}
