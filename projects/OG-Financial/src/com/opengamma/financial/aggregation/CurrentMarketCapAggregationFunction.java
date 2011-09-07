/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.id.ExternalIdBundle;

/**
 * Function to classify positions by Currency.
 *
 */
public class CurrentMarketCapAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Market Cap";
  private static final String FIELD = "CUR_MKT_CAP";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_CUR_MKT_CAP = "N/A";

  private HistoricalTimeSeriesSource _htsSource;
  private SecuritySource _secSource;
  
  public CurrentMarketCapAggregationFunction(SecuritySource secSource, HistoricalTimeSeriesSource htsSource) {
    _secSource = secSource;
    _htsSource = htsSource;
  }
  
  private EquitySecurityVisitor<String> _equitySecurityVisitor = new EquitySecurityVisitor<String>() {
    @Override
    public String visitEquitySecurity(EquitySecurity security) {
      return getCurrentMarketCap(security.getExternalIdBundle());
    }
  };
  
  private EquityOptionSecurityVisitor<String> _equityOptionSecurityVisitor = new EquityOptionSecurityVisitor<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      EquitySecurity underlying = (EquitySecurity) _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      return getCurrentMarketCap(underlying.getExternalIdBundle());
    }    
  };
  
  private String getCurrentMarketCap(ExternalIdBundle externalIdBundle) {
    try {
      LocalDate yesterday = Clock.systemDefaultZone().yesterday();
      LocalDate oneWeekAgo = yesterday.minusDays(7);
      HistoricalTimeSeries historicalTimeSeries = _htsSource.getHistoricalTimeSeries(FIELD, externalIdBundle, 
                                                                                     RESOLUTION_KEY, oneWeekAgo, true, yesterday, true);
      if (historicalTimeSeries != null && historicalTimeSeries.getTimeSeries() != null && !historicalTimeSeries.getTimeSeries().isEmpty()) {
        double currentMarketCap = historicalTimeSeries.getTimeSeries().getLatestValue();
        if (currentMarketCap < 0.2) {
          return "< 0.2";
        } else if (currentMarketCap < 0.5) {
          return "0.2 - 0.5";
        } else if (currentMarketCap < 1.0) {
          return "0.5 - 1";
        } else if (currentMarketCap < 3) {
          return "1 - 3";
        } else if (currentMarketCap < 10) {
          return "3 - 10";
        } else {
          return "10+";
        }
      } else {
        return NO_CUR_MKT_CAP;
      }
    } catch (UnsupportedOperationException ex) {
      return NO_CUR_MKT_CAP;
    }
  }
  
  @Override
  public String classifyPosition(Position position) {
    FinancialSecurityVisitorAdapter<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
                                                                                            .equitySecurityVisitor(_equitySecurityVisitor)
                                                                                            .equityOptionVisitor(_equityOptionSecurityVisitor)
                                                                                            .create();
    FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
    String classification = security.accept(visitorAdapter);
    return classification == null ? "Unknown" : classification;
  }

  public String getName() {
    return NAME;
  }
}
