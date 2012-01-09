/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.Pair;

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
  
  private static final String NANO_CAP = "Nano Cap";
  private static final String MICRO_CAP = "Micro Cap";
  private static final String SMALL_CAP = "Small Cap";
  private static final String MID_CAP = "Mid Cap";
  private static final String LARGE_CAP = "Large Cap";
  
  private static final List<String> REQUIRED = Arrays.asList(LARGE_CAP, MID_CAP, SMALL_CAP, MICRO_CAP, NANO_CAP, NO_CUR_MKT_CAP);

  private HistoricalTimeSeriesSource _htsSource;
  private SecuritySource _secSource;
  private boolean _caching = true;
  private Map<UniqueId, Double> _currMktCapCache = new HashMap<UniqueId, Double>();

  public CurrentMarketCapAggregationFunction(SecuritySource secSource, HistoricalTimeSeriesSource htsSource, boolean useAttributes) {
    _secSource = secSource;
    _htsSource = htsSource;
    _useAttributes = useAttributes;
  }

  
  public CurrentMarketCapAggregationFunction(SecuritySource secSource, HistoricalTimeSeriesSource htsSource) {
    this(secSource, htsSource, true);
  }
  
  private EquitySecurityVisitor<Double> _equitySecurityVisitor = new EquitySecurityVisitor<Double>() {
    @Override
    public Double visitEquitySecurity(EquitySecurity security) {
      return getCurrentMarketCap(security);
    }
  };
  
  private EquityOptionSecurityVisitor<Double> _equityOptionSecurityVisitor = new EquityOptionSecurityVisitor<Double>() {
    @Override
    public Double visitEquityOptionSecurity(EquityOptionSecurity security) {
      EquitySecurity underlying = (EquitySecurity) _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      return getCurrentMarketCap(underlying);
    }    
  };
  
  protected Double getCurrentMarketCap(Security security) {
    try {
      if (_caching && security.getUniqueId() != null) {
        if (_currMktCapCache.containsKey(security.getUniqueId())) {
          return _currMktCapCache.get(security.getUniqueId());
        }
      }
      ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
      Pair<LocalDate, Double> latest = _htsSource.getLatestDataPoint(FIELD, externalIdBundle, RESOLUTION_KEY);
      if (latest != null && latest.getValue() != null) {
        _currMktCapCache.put(security.getUniqueId(), latest.getValue());
        return latest.getValue();
      } else {
        _currMktCapCache.put(security.getUniqueId(), null);
        return null;
      }
    } catch (UnsupportedOperationException ex) {
      return null;
    }
  }
  
  private String getCurrentMarketCapCategory(Double currentMarketCap) {
    if (currentMarketCap != null) {
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
      FinancialSecurityVisitorAdapter<Double> visitorAdapter = FinancialSecurityVisitorAdapter.<Double>builder()
                                                                                              .equitySecurityVisitor(_equitySecurityVisitor)
                                                                                              .equityOptionVisitor(_equityOptionSecurityVisitor)
                                                                                              .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      Double currMarketCap = security.accept(visitorAdapter);
      String classification = getCurrentMarketCapCategory(currMarketCap); 
      return classification == null ? NO_CUR_MKT_CAP : classification;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return REQUIRED;
  }


  @Override
  public int compare(String marketCapBucket1, String marketCapBucket2) {
    return CompareUtils.compareByList(REQUIRED, marketCapBucket1, marketCapBucket2);
  }
  
  private class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(Position position1, Position position2) {
      FinancialSecurityVisitorAdapter<Double> visitorAdapter = FinancialSecurityVisitorAdapter.<Double>builder()
          .equitySecurityVisitor(_equitySecurityVisitor)
          .equityOptionVisitor(_equityOptionSecurityVisitor)
          .create();
      FinancialSecurity security1 = (FinancialSecurity) position1.getSecurityLink().resolve(_secSource);
      FinancialSecurity security2 = (FinancialSecurity) position2.getSecurityLink().resolve(_secSource);
      Double currMktCap1 = security1.accept(visitorAdapter);
      Double currMktCap2 = security2.accept(visitorAdapter);
      return CompareUtils.compareWithNullLow(currMktCap1, currMktCap2);
    }
  }


  @Override
  public Comparator<Position> getPositionComparator() {
    return new PositionComparator();
  }
  
}
