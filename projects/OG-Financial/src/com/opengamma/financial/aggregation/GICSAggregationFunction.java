/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.id.ExternalIdBundle;

/**
 * Abstract aggregation function for bucketing equities and equity options by GICS code of the underlying
 */
public class GICSAggregationFunction implements AggregationFunction<String> {

  private static final String UNKNOWN = "Unknown";
  private boolean _useAttributes;

  /**
   * Enumerated type representing how specific the GICS code should be interpreted.
   */
  public enum Level { SECTOR, INDUSTRY_GROUP, INDUSTRY, SUB_INDUSTRY }

  private Level _level;
  private SecuritySource _secSource;;
  
  /* to make dep injection easier */
  public GICSAggregationFunction(SecuritySource secSource, String levelString, String useAttributes) {
    this(secSource, Level.valueOf(levelString));
  }
  
  public GICSAggregationFunction(SecuritySource secSource, Level level) {
    this(secSource, level, true);
  }
  
  public GICSAggregationFunction(SecuritySource secSource, Level level, boolean useAttributes) {
    _secSource = secSource;
    _level = level;
    _useAttributes = useAttributes;
  }
  
  private EquitySecurityVisitor<String> _equitySecurityVisitor = new EquitySecurityVisitor<String>() {
    @Override
    public String visitEquitySecurity(EquitySecurity security) {
      if (security.getGicsCode() != null) {
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
      }
      return UNKNOWN;
    }
  };
  
  private EquityOptionSecurityVisitor<String> _equityOptionSecurityVisitor = new EquityOptionSecurityVisitor<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      EquitySecurity underlying = (EquitySecurity) _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying.getGicsCode() != null) {
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
      }
      return UNKNOWN;
    }    
  };
  
  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return UNKNOWN;
      }
    } else {
      FinancialSecurityVisitorAdapter<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
                                                                                              .equitySecurityVisitor(_equitySecurityVisitor)
                                                                                              .equityOptionVisitor(_equityOptionSecurityVisitor)
                                                                                              .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      String classification = security.accept(visitorAdapter);
      return classification == null ? UNKNOWN : classification;
    }
  }

  @Override
  public String getName() {
    return "GICS by " + _level;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    Collection<String> baseList = new ArrayList<String>();
    switch (_level) {
      case SECTOR:
        baseList.addAll(GICSCode.getAllSectorDescriptions());
        break;
      case INDUSTRY_GROUP:
        baseList.addAll(GICSCode.getAllIndustryGroupDescriptions());
        break;
      case INDUSTRY:
        baseList.addAll(GICSCode.getAllIndustryDescriptions());
        break;
      case SUB_INDUSTRY:
        baseList.addAll(GICSCode.getAllSubIndustryDescriptions());
        break;
    }
    baseList.add(UNKNOWN);
    return baseList;
  }

}
