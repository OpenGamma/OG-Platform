/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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

  private boolean _useAttributes;
  private static final String NAME = "Market Cap";
  private static final String FIELD = "CUR_MKT_CAP";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_CUR_MKT_CAP = "N/A";
  
  private static final double NANO_CAP_UPPER_THRESHOLD = 10;
  private static final double MICRO_CAP_UPPER_THRESHOLD = 100;
  private static final double SMALL_CAP_UPPER_THRESHOLD = 1000;
  private static final double MID_CAP_UPPER_THRESHOLD = 10E3;
  //private static final double LARGE_CAP_UPPER_THRESHOLD = 100E3;
  
  private static final String NANO_CAP = "E) Nano Cap";
  private static final String MICRO_CAP = "D) Micro Cap";
  private static final String SMALL_CAP = "C) Small Cap";
  private static final String MID_CAP = "B) Mid Cap";
  private static final String LARGE_CAP = "A) Large Cap";

  private HistoricalTimeSeriesSource _htsSource;
  private SecuritySource _secSource;

  public CurrentMarketCapAggregationFunction(SecuritySource secSource, HistoricalTimeSeriesSource htsSource, boolean useAttributes) {
    _secSource = secSource;
    _htsSource = htsSource;
    _useAttributes = useAttributes;
  }

  
  public CurrentMarketCapAggregationFunction(SecuritySource secSource, HistoricalTimeSeriesSource htsSource) {
    this(secSource, htsSource, true);
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
        if (currentMarketCap < NANO_CAP_UPPER_THRESHOLD) {
          return NANO_CAP;
        } else if (currentMarketCap < MICRO_CAP_UPPER_THRESHOLD) {
          return MICRO_CAP;
        } else if (currentMarketCap < SMALL_CAP_UPPER_THRESHOLD) {
          return SMALL_CAP;
        } else if (currentMarketCap < MID_CAP_UPPER_THRESHOLD) {
          return MID_CAP;
        } else {
          return LARGE_CAP;
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
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NO_CUR_MKT_CAP;
      }
    } else {
      FinancialSecurityVisitorAdapter<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
                                                                                              .equitySecurityVisitor(_equitySecurityVisitor)
                                                                                              .equityOptionVisitor(_equityOptionSecurityVisitor)
                                                                                              .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      String classification = security.accept(visitorAdapter);
      return classification == null ? NO_CUR_MKT_CAP : classification;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Arrays.<String>asList(new String[] {LARGE_CAP, MID_CAP, SMALL_CAP, MICRO_CAP, NANO_CAP, NO_CUR_MKT_CAP});
  }
}
