/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class UnderlyingTypeToHistoricalTimeSeries {
  private static final String LAST_PRICE = "PX_LAST";

  //private static final String IMPLIED_VOLATILITY = "OPT_IMPLIED_VOLATILITY_BST";
  //private static final String VOLUME = "VOLUME";

  public static LocalDateDoubleTimeSeries getSeries(final HistoricalTimeSeriesSource source, final String dataSourceName, final String dataProviderName, final SecuritySource secMaster,
      final UnderlyingType underlying, final Security security) {
    if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity option = (EquityOptionSecurity) security;
      switch (underlying) {
        case SPOT_PRICE:
          final Security underlyingSecurity = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier()));
          final HistoricalTimeSeries hts = source.getHistoricalTimeSeries(underlyingSecurity.getIdentifiers(), dataSourceName, dataProviderName, LAST_PRICE);
          if (hts == null) {
            throw new NullPointerException("Could not get time series pair for " + underlying + " for security " + security);
          }
          return hts.getTimeSeries();
        default:
          throw new NotImplementedException("Don't know how to time series for " + underlying);
      }
    }
    throw new NotImplementedException("Can only get time series for options");
  }
}
