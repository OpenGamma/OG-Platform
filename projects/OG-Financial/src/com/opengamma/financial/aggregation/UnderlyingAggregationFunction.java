/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurityVisitor;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurityVisitor;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Abstract aggregation function for bucketing equities and equity options by GICS code of the underlying
 */
public class UnderlyingAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
  
  private ExternalScheme _preferredScheme;
  private SecuritySource _secSource;;
  private Comparator<Position> _comparator = new SimplePositionComparator();
  
  private static final String NOT_APPLICABLE = "N/A";
  /* to make dep injection easier */
  public UnderlyingAggregationFunction(SecuritySource secSource, String preferredSchemeString) {
    this(secSource, ExternalScheme.of(preferredSchemeString));
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, String preferredSchemeString, boolean useAttributes) {
    this(secSource, ExternalScheme.of(preferredSchemeString), useAttributes);
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, ExternalScheme preferredScheme) {
    this(secSource, preferredScheme, true);
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, ExternalScheme preferredScheme, boolean useAttributes) {
    _secSource = secSource;
    _preferredScheme = preferredScheme;
    _useAttributes = useAttributes;
  }
  
  private EquityIndexOptionSecurityVisitor<String> _equityIndexOptionSecurityVisitor = new EquityIndexOptionSecurityVisitor<String>() {
    @Override
    public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      Security underlying = _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
  };
  
  private EquityOptionSecurityVisitor<String> _equityOptionSecurityVisitor = new EquityOptionSecurityVisitor<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      Security underlying = _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
      return identifier != null ? identifier : NOT_APPLICABLE;    
    }    
  };
  
  private FXOptionSecurityVisitor<String> _fxOptionSecurityVisitor = new FXOptionSecurityVisitor<String>() {
    @Override
    public String visitFXOptionSecurity(FXOptionSecurity fxOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(), fxOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  private FXBarrierOptionSecurityVisitor<String> _fxBarrierOptionSecurityVisitor = new FXBarrierOptionSecurityVisitor<String>() {
    @Override
    public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity fxBarrierOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxBarrierOptionSecurity.getCallCurrency(), fxBarrierOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  private IRFutureOptionSecurityVisitor<String> _irFutureOptionSecurityVisitor = new IRFutureOptionSecurityVisitor<String>() {
    @Override
    public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      Security underlying = _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
  };
  
  private SwaptionSecurityVisitor<String> _swaptionSecurityVisitor = new SwaptionSecurityVisitor<String>() {
    public String visitSwaptionSecurity(SwaptionSecurity security) {
      SwapSecurity underlying = (SwapSecurity) _secSource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      String name = underlying.getName();
      return (name != null && name.length() > 0) ? name : NOT_APPLICABLE;
    }    
  };

  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NOT_APPLICABLE;
      } 
    } else {
      FinancialSecurityVisitorAdapter<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
                                                                                              .equityIndexOptionVisitor(_equityIndexOptionSecurityVisitor)
                                                                                              .equityOptionVisitor(_equityOptionSecurityVisitor)
                                                                                              .fxOptionVisitor(_fxOptionSecurityVisitor)
                                                                                              .fxBarrierOptionVisitor(_fxBarrierOptionSecurityVisitor)
                                                                                              .irfutureOptionVisitor(_irFutureOptionSecurityVisitor)
                                                                                              .swaptionVisitor(_swaptionSecurityVisitor)
                                                                                              .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      String classification = security.accept(visitorAdapter);
      return classification == null ? NOT_APPLICABLE : classification;
    }
  }

  @Override
  public String getName() {
    return "Underlying";
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(String o1, String o2) {
    if (o1.equals(NOT_APPLICABLE)) {
      if (o2.equals(NOT_APPLICABLE)) {
        return 0;
      }
      return 1;
    } else if (o2.equals(NOT_APPLICABLE)) {
      return -1;
    }
    return o1.compareTo(o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

}
