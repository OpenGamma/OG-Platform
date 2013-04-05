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
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Aggregation function for bucketing securities by their underlying hedging instrument.
 * Originally, this was used to bucket equity options by GICS code of the underlying equity, but has since been expanded.
 */
public class UnderlyingAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
  
  private ExternalScheme _preferredScheme;
  private SecuritySource _secSource;
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
    this(secSource, preferredScheme, false);
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, ExternalScheme preferredScheme, boolean useAttributes) {
    _secSource = secSource;
    _preferredScheme = preferredScheme;
    _useAttributes = useAttributes;
  }
  

  // CommodityFutureOptionSecurity
  private FinancialSecurityVisitor<String> _commodityFutureOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
      Security underlying = _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }    
  };
  
  private FinancialSecurityVisitor<String> _equityIndexOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      //Security underlying = _secSource.get(ExternalIdBundle.of(security.getUnderlyingId()));
      // we could use a historical time series source to look up the bundle at this point.
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
  };

  private FinancialSecurityVisitor<String> _equityIndexFutureOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security) {
      //Security underlying = _secSource.get(ExternalIdBundle.of(security.getUnderlyingId()));
      // we could use a historical time series source to look up the bundle at this point.
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
  };
  
  private FinancialSecurityVisitor<String> _equityOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityOptionSecurity(EquityOptionSecurity security) {
      Security underlying = _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }    
  };
  
  private FinancialSecurityVisitor<String> _equityBarrierOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
      Security underlying = _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  };
  
  private FinancialSecurityVisitor<String> _fxOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitFXOptionSecurity(FXOptionSecurity fxOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(), fxOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  private FinancialSecurityVisitor<String> _equitySecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquitySecurity(EquitySecurity equitySecurity) {
      String ticker = equitySecurity.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  /*   // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FIXME MANY FUTURES ARE MISSING 
  private FinancialSecurityVisitor<String> _futureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
//  private FinancialSecurityVisitorSameMethodAdapter<String> _futureSecurityVisitor = new FinancialSecurityVisitorSameMethodAdapter<String>() {
    @Override
    public String visitFutureSecurity(FutureSecurity futureSecurity) {
      String ticker = futureSecurity.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
 */
  
  @Deprecated
  private FinancialSecurityVisitor<String> _agricultureFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _metalFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitMetalFutureSecurity(MetalFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };

  @Deprecated
  private FinancialSecurityVisitor<String> _bondFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitBondFutureSecurity(BondFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _energyFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _deliverableSwapFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _equityFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityFutureSecurity(EquityFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _equityIndexDividendFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _fxFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitFXFutureSecurity(FXFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _indexFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitIndexFutureSecurity(IndexFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _interestRateFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  @Deprecated
  private FinancialSecurityVisitor<String> _stockFutureSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitStockFutureSecurity(StockFutureSecurity security) {
      String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
      return ticker != null ? ticker : NOT_APPLICABLE;
    }
  };
  
  private FinancialSecurityVisitor<String> _ndfFxOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity fxOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(), fxOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  private FinancialSecurityVisitor<String> _fxDigitalOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitFXDigitalOptionSecurity(FXDigitalOptionSecurity fxOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(), fxOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  
  private FinancialSecurityVisitor<String> _ndfFxDigitalOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity fxOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(), fxOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  
  private FinancialSecurityVisitor<String> _fxBarrierOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity fxBarrierOptionSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxBarrierOptionSecurity.getCallCurrency(), fxBarrierOptionSecurity.getPutCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
  private FinancialSecurityVisitor<String> _fxForwardSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitFXForwardSecurity(FXForwardSecurity fxForwardSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxForwardSecurity.getPayCurrency(), fxForwardSecurity.getReceiveCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
   
  private FinancialSecurityVisitor<String> _fxNdfForwardSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity ndfFxForwardSecurity) {
      UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(ndfFxForwardSecurity.getPayCurrency(), ndfFxForwardSecurity.getReceiveCurrency());
      return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
    }
  };
  
    
  private FinancialSecurityVisitor<String> _irFutureOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    @Override
    public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      Security underlying = _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
  };
  
  private FinancialSecurityVisitor<String> _swaptionSecurityVisitor = new FinancialSecurityVisitorAdapter<String>() {
    public String visitSwaptionSecurity(SwaptionSecurity security) {
      SwapSecurity underlying = (SwapSecurity) _secSource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
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
      FinancialSecurityVisitor<String> visitorAdapter = FinancialSecurityVisitorAdapter.<String>builder()
          .commodityFutureOptionSecurityVisitor(_commodityFutureOptionSecurityVisitor)
          // .futureSecurityVisitor(_futureSecurityVisitor) // TODO: MANY FUTURES ARE MISSING !!!
          .agricultureFutureSecurityVisitor(_agricultureFutureSecurityVisitor)
          .metalFutureSecurityVisitor(_metalFutureSecurityVisitor)
          .bondFutureSecurityVisitor(_bondFutureSecurityVisitor)
          .energyFutureSecurityVisitor(_energyFutureSecurityVisitor)
          .equityFutureSecurityVisitor(_equityFutureSecurityVisitor)
          .equityIndexDividendFutureSecurityVisitor(_equityIndexDividendFutureSecurityVisitor)
          .fxFutureSecurityVisitor(_fxFutureSecurityVisitor)
          .indexFutureSecurityVisitor(_indexFutureSecurityVisitor)
          .interestRateFutureSecurityVisitor(_interestRateFutureSecurityVisitor)
          .stockFutureSecurityVisitor(_stockFutureSecurityVisitor)
          .equitySecurityVisitor(_equitySecurityVisitor)
          .equityIndexOptionVisitor(_equityIndexOptionSecurityVisitor)
          .equityOptionVisitor(_equityOptionSecurityVisitor)
          .equityBarrierOptionVisitor(_equityBarrierOptionSecurityVisitor)
          .fxForwardVisitor(_fxForwardSecurityVisitor)
          .nonDeliverableFxForwardVisitor(_fxNdfForwardSecurityVisitor)
          .fxOptionVisitor(_fxOptionSecurityVisitor)
          .nonDeliverableFxOptionVisitor(_ndfFxOptionSecurityVisitor)
          .fxDigitalOptionVisitor(_fxDigitalOptionSecurityVisitor)
          .fxNonDeliverableDigitalOptionVisitor(_ndfFxDigitalOptionSecurityVisitor)
          .fxBarrierOptionVisitor(_fxBarrierOptionSecurityVisitor)
          .irfutureOptionVisitor(_irFutureOptionSecurityVisitor)
          .swaptionVisitor(_swaptionSecurityVisitor)
          .equityIndexFutureOptionVisitor(_equityIndexFutureOptionSecurityVisitor)
          .create();
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
      try {
        String classification = security.accept(visitorAdapter);
        return classification == null ? NOT_APPLICABLE : classification;
      } catch (UnsupportedOperationException uoe) {
        return NOT_APPLICABLE;
      }
      
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
