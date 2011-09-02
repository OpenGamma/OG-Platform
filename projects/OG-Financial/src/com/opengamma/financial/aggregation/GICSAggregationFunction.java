/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

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
 * Abstract aggregation function for bucketing equities and equity options by GICS code of the underlying
 */
public class GICSAggregationFunction implements AggregationFunction<String> {

  /**
   * Enumerated type representing how specific the GICS code should be interpreted.
   */
  public enum Level { SECTOR, INDUSTRY_GROUP, INDUSTRY, SUB_INDUSTRY }

  private Level _level;
  private SecuritySource _secSource;;
  
  /* to make dep injection easier */
  public GICSAggregationFunction(SecuritySource secSource, String levelString) {
    this(secSource, Level.valueOf(levelString));
  }
  
  public GICSAggregationFunction(SecuritySource secSource, Level level) {
    _secSource = secSource;
    _level = level;
  }
  
  private EquitySecurityVisitor<String> _equitySecurityVisitor = new EquitySecurityVisitor<String>() {
    @Override
    public String visitEquitySecurity(EquitySecurity security) {
      switch (_level) {
        case SECTOR:
          return security.getGicsCode().getSectorDescription();
        case INDUSTRY_GROUP:
          return security.getGicsCode().getIndustryGroupDescription();
        case INDUSTRY:
          return security.getGicsCode().getIndustryDescription();
        case SUB_INDUSTRY:
          return security.getGicsCode().getSubIndustryDescription();
      }
      return "Unknown";
    }
  };
  
  private EquityOptionSecurityVisitor<String> _equityOptionSecurityVisitor = new EquityOptionSecurityVisitor<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      EquitySecurity underlying = (EquitySecurity) _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingIdentifier()));
      switch (_level) {
        case SECTOR:
          return underlying.getGicsCode().getSectorDescription();
        case INDUSTRY_GROUP:
          return underlying.getGicsCode().getIndustryGroupDescription();
        case INDUSTRY:
          return underlying.getGicsCode().getIndustryDescription();
        case SUB_INDUSTRY:
          return underlying.getGicsCode().getSubIndustryDescription();
      }
      return "Unknown";
    }    
  };
  
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

  @Override
  public String getName() {
    return "GICS by " + _level;
  }

}
