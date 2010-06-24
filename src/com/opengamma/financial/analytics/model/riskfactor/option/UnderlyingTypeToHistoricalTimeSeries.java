/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

public class UnderlyingTypeToHistoricalTimeSeries {
  private static final String DATA_SOURCE = "BLOOMBERG"; // crap passed to the BloombergHistoricalDataProvider
  private static final String DATA_PROVIDER = null; // crap passed to the BloombergHistoricalDataProvider 
  private static final String LAST_PRICE = "PX_LAST";
  @SuppressWarnings("unused")
  private static final String IMPLIED_VOLATILITY = "OPT_IMPLIED_VOLATILITY_BST";
  @SuppressWarnings("unused")
  private static final String VOLUME = "VOLUME";
  
  public static LocalDateDoubleTimeSeries getSeries(final HistoricalDataProvider dataProvider, final SecurityMaster secMaster, final UnderlyingType underlying, final Security security) {
    if (security instanceof OptionSecurity) {
      final OptionSecurity option = (OptionSecurity) security;
      switch (underlying) {
        case SPOT_PRICE:
          Security underlyingSecurity = secMaster.getSecurity(new IdentifierBundle(option.getUnderlyingIdentifier()));
          LocalDateDoubleTimeSeries timeSeries = dataProvider.getHistoricalTimeSeries(underlyingSecurity.getIdentifiers(), DATA_SOURCE, DATA_PROVIDER, LAST_PRICE);
          return timeSeries;
        default:
          throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
      }
    }
    throw new NotImplementedException("Can only get ValueRequirements for options");
  }
}
